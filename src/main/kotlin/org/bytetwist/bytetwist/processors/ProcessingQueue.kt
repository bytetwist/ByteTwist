package org.bytetwist.bytetwist.processors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.bytetwist.bytetwist.nodes.*
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Runs each processor sequentially over the entire node list before proceeding to the next processor.
 */
@InternalCoroutinesApi
open class ProcessingQueue {

    val methodNodes = CopyOnWriteArraySet<ByteMethod>()
    val fieldNodes = CopyOnWriteArraySet<ByteField>()
    var processors = sequenceOf<AbstractNodeProcessor<in ByteNode>>().constrainOnce()

    /**
     * Adds a processor to the end of the processing queue.
     */
    fun <T : ByteNode> addProcessor(processor: AbstractNodeProcessor<in T>) {
        processors += processor as AbstractNodeProcessor<in ByteNode>
    }

    @ExperimentalCoroutinesApi
    private fun classes() = flow {
        this.emitAll(Companion.nodes.asFlow())
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun fields() = flow {
        this.emitAll(Companion.nodes.flatMap { compiledClass -> compiledClass.fields as Iterable<ByteField> }.asFlow())
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun methods(): Flow<ByteMethod> = flow {
        this.emitAll(Companion.nodes.flatMap { c -> c.methods.filterIsInstance(ByteMethod::class.java) }.asFlow())
    }

    @ExperimentalCoroutinesApi
    fun blocks(): Flow<ByteBlockNode> = flow {
        methods().onEach { byteMethod -> byteMethod.blocks.onEach { emit(it) } }
    }

    @ExperimentalCoroutinesApi
    fun annotations(): Flow<ByteAnnotation> = flow {
        this.emitAll(listOf(Companion.nodes.flatMap { c -> c.visibleAnnotations as Iterable<ByteAnnotation> }).flatten().asFlow())
        this.emitAll(Companion.nodes.flatMap { compiledClass ->
            compiledClass.fields.flatMap { fieldNode ->
               fieldNode.visibleAnnotations as List<ByteAnnotation>
            }
        }.asFlow())

    }

    @ExperimentalCoroutinesApi
    fun fieldRefs(): Flow<FieldReferenceNode> = flow {
        this.emitAll(Companion.nodes.flatMap { c ->
            c.methods.flatMap { node ->
                (node as ByteMethod).instructions.filterIsInstance(
                    FieldReferenceNode::class.java
                )
            }
                    as Iterable<FieldReferenceNode>
        }.asFlow())
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun methodRefs(): Flow<MethodReferenceNode> = flow {
        this.emitAll(Companion.nodes.flatMap { c ->
            c.methods.flatMap { node ->
                (node as ByteMethod).instructions.filterIsInstance(
                    MethodReferenceNode::class.java
                )
            }
                    as Iterable<MethodReferenceNode>
        }.asFlow())
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
                    ByteBlockNode::class -> processor.subscribe(blocks())
                }
                processor.complete()
            }
        }
    }

    companion object {
        var nodes = CopyOnWriteArraySet<ByteClass>()
    }
}

