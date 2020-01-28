package org.bytetwist.bytetwist.processors

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bytetwist.bytetwist.nodes.*
import org.bytetwist.bytetwist.processors.common.JarOutputProcessor
import java.lang.IllegalStateException
import java.util.concurrent.CopyOnWriteArraySet
import javax.annotation.processing.AbstractProcessor
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Runs each processor sequentially over the entire node list before proceeding to the next processor.
 */
@InternalCoroutinesApi
open class ProcessingQueue {

    val nodes = CopyOnWriteArraySet<CompiledClass>()
    val methodNodes = CopyOnWriteArraySet<CompiledMethod>()
    val fieldNodes = CopyOnWriteArraySet<CompiledField>()
    var processors = sequenceOf<AbstractNodeProcessor<in CompiledNode>>().constrainOnce()

    /**
     * Adds a processor to the end of the processing queue.
     */
    fun <T : CompiledNode> addProcessor(processor: AbstractNodeProcessor<in T>) {
        processors += processor as AbstractNodeProcessor<in CompiledNode>
    }

    @ExperimentalCoroutinesApi
    private fun classes() = flow {
        this.emitAll(nodes.asFlow())
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun fields() = flow {
        this.emitAll(nodes.flatMap { compiledClass -> compiledClass.fields as Iterable<CompiledField> }.asFlow())
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun methods(): Flow<CompiledMethod> = flow {
        this.emitAll(nodes.flatMap { c -> c.methods.filterIsInstance(CompiledMethod::class.java) as Iterable<CompiledMethod> }.asFlow())
    }

    @ExperimentalCoroutinesApi
    fun annotations(): Flow<CompiledAnnotation> = flow {
        this.emitAll(listOf(nodes.flatMap { c -> c.visibleAnnotations as Iterable<CompiledAnnotation> }).flatten().asFlow())
        this.emitAll(nodes.flatMap { compiledClass ->
            compiledClass.fields.flatMap { fieldNode ->
               fieldNode.visibleAnnotations as List<CompiledAnnotation>
            }
        }.asFlow())

    }

    @ExperimentalCoroutinesApi
    fun fieldRefs(): Flow<FieldReferenceNode> = flow {
        this.emitAll(nodes.flatMap { c ->
            c.methods.flatMap { node ->
                (node as CompiledMethod).instructions.filterIsInstance(
                    FieldReferenceNode::class.java
                )
            }
                    as Iterable<FieldReferenceNode>
        }.asFlow())
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun methodRefs(): Flow<MethodReferenceNode> = flow {
        this.emitAll(nodes.flatMap { c ->
            c.methods.flatMap { node ->
                (node as CompiledMethod).instructions.filterIsInstance(
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
                    CompiledClass::class -> processor.subscribe(classes())
                    CompiledMethod::class -> processor.subscribe(methods())
                    ConstructorNode::class -> processor.subscribe(methods().filterIsInstance<ConstructorNode>())
                    CompiledField::class -> processor.subscribe(fields())
                    FieldReferenceNode::class -> processor.subscribe(fieldRefs())
                    FieldWrite::class -> processor.subscribe(fieldRefs().filterIsInstance<FieldWrite>())
                    FieldRead::class -> processor.subscribe(fieldRefs().filterIsInstance<FieldRead>())
                    MethodReferenceNode::class -> processor.subscribe(methodRefs())
                    CompiledAnnotation::class -> processor.subscribe(annotations())
                }
                processor.complete()
            }
        }
    }
}

