package org.bytetwist.bytetwist.processors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.nodes.*
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
    @InternalCoroutinesApi
    fun methods(): Flow<ByteMethod> = flow {
        this.emitAll(References.methodNames.values.asFlow())
    }

    @ExperimentalCoroutinesApi
    fun blocks(): Flow<ByteBlockNode> = flow {
        methods().onEach { byteMethod ->
            byteMethod.blocks.onEach { byteBlockNode ->
                emit(byteBlockNode)
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun annotations(): Flow<ByteAnnotation> = flow {
        methods().onEach { byteMethod ->
            byteMethod.visibleAnnotations.onEach {
                emit(it as ByteAnnotation)
            }
        }
        classes().onEach { byteClass ->
            byteClass.visibleAnnotations.onEach {
                emit(it as ByteAnnotation)
            }
        }
        fields().onEach {byteField ->
            byteField.visibleAnnotations.onEach {
                emit(it as FieldAnnotationNode)
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun classAnnotations() = annotations().filter {
        it is ClassAnnotationNode } as Flow<ClassAnnotationNode>

    @ExperimentalCoroutinesApi
    fun fieldAnnotations() = annotations().filter {
        it is FieldAnnotationNode } as Flow<FieldAnnotationNode>

    @ExperimentalCoroutinesApi
    fun methodAnnotations() = annotations().filter {
        it is MethodAnnotationNode } as Flow<MethodAnnotationNode>

    @ExperimentalCoroutinesApi
    fun fieldRefs(): Flow<FieldReferenceNode> = flow {
        methods().onEach { byteBlockNode ->
            emitAll(byteBlockNode.instructions.filterIsInstance<FieldReferenceNode>().asFlow())
        }
    } as Flow<FieldReferenceNode>

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
                    ByteConstructor::class -> processor.subscribe(methods().filterIsInstance<ByteConstructor>())
                    ByteField::class -> processor.subscribe(fields())
                    FieldReferenceNode::class -> processor.subscribe(fieldRefs())
                    FieldWrite::class -> processor.subscribe(fieldRefs().filterIsInstance<FieldWrite>())
                    FieldRead::class -> processor.subscribe(fieldRefs().filterIsInstance<FieldRead>())
                    MethodReferenceNode::class -> processor.subscribe(methodRefs())
                    ByteAnnotation::class -> processor.subscribe(annotations())
                    ClassAnnotationNode::class -> processor.subscribe(classAnnotations())
                    FieldAnnotationNode::class -> processor.subscribe(fieldAnnotations())
                    MethodAnnotationNode::class -> processor.subscribe(methodAnnotations())
                    ByteBlockNode::class -> processor.subscribe(blocks())
                    ByteNode::class -> processor.subscribe(flow<ByteNode> {
                        emitAll(annotations())
                        emitAll(classes())
                        emitAll(methods())
                        emitAll(fields())
                        emitAll(fieldRefs())
                        emitAll(methodRefs())
                        emitAll(blocks())

                    })
                }

                processor.complete()
            }
        }
    }
}

