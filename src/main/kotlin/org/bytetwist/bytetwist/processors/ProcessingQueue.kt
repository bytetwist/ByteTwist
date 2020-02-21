package org.bytetwist.bytetwist.processors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.nodes.*
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Runs each processor sequentially over the entire node list before proceeding to the next processor.
 */
@InternalCoroutinesApi
open class ProcessingQueue() {

    var processors = sequenceOf<AbstractNodeProcessor<in ByteNode>>().constrainOnce()
    val nodes = CopyOnWriteArrayList<ByteClass>()
    /**
     * Adds a processor to the end of the processing queue.
     */
    fun <T : ByteNode> addProcessor(processor: AbstractNodeProcessor<in T>) {
        processors += processor as AbstractNodeProcessor<in ByteNode>
    }

    @ExperimentalCoroutinesApi
    private fun classes() = flow {
        this.emitAll(nodes.asFlow())
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun fields() = flow {
        this.emitAll(References.fieldNames.values.asFlow())
    }

    @ExperimentalCoroutinesApi
    val blocks = flow {
        emitAll(References.blocks.asFlow())
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun methods(): Flow<ByteMethod> = flow {
        this.emitAll(References.methodNames.values.asFlow())
    }

    @ExperimentalCoroutinesApi
    fun annotations(): Flow<ByteAnnotation> = flow {
        methods().onEach { byteMethod -> byteMethod.visibleAnnotations.onEach { emit(it as ByteAnnotation) } }
        }


    @ExperimentalCoroutinesApi
    fun fieldRefs(): Flow<FieldReferenceNode> = flow {
        this.emitAll(References.fieldReferences.asFlow())
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun methodRefs(): Flow<MethodReferenceNode> = flow {
        methods().onEach { emitAll(it.methodCalls().asFlow()) }
    }


    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun process() {
        with(processors.iterator()) {
            while (this.hasNext()) {
                val processor = this.next()
                when (processor.type) {
                    ByteClass::class -> processor.subscribe(classes())
                    ByteMethod::class -> processor.subscribe(methods())
                    ConstructorNode::class -> processor.subscribe(methods().filterIsInstance<ConstructorNode>())
                    ByteField::class -> processor.subscribe(fields())
                    FieldReferenceNode::class -> processor.subscribe(fieldRefs())
                    FieldWrite::class -> processor.subscribe(fieldRefs().filterIsInstance<FieldWrite>())
                    FieldRead::class -> processor.subscribe(fieldRefs().filterIsInstance<FieldRead>())
                    MethodReferenceNode::class -> processor.subscribe(methodRefs())
                    ByteAnnotation::class -> processor.subscribe(annotations())
                    Block::class -> processor.subscribe(blocks)
                }
                processor.complete()
            }
        }
    }
}

