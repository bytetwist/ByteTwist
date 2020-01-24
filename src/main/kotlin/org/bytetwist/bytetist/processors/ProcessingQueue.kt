package org.bytetwist.bytetist.processors

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bytetwist.bytetist.nodes.*
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Runs each processor sequentially over the entire node list before proceeding to the next processor.
 */
open class ProcessingQueue {

    val nodes = CopyOnWriteArraySet<CompiledClass>()
    val methodNodes = CopyOnWriteArraySet<CompiledMethod>()
    val fieldNodes = CopyOnWriteArraySet<CompiledField>()
    var processors = arrayListOf<AbstractProcessor<in CompiledNode>>()

    /**
     * Adds a processor to the end of the processing queue.
     */
   fun <T : CompiledNode> addProcessor(processor: AbstractProcessor<in T>) {
        processors.add(processor as AbstractProcessor<in CompiledNode>)
    }

    private fun classes() = flow {
        this.emitAll(nodes.asFlow())
    }

    @InternalCoroutinesApi
    fun fields() = flow {
        this.emitAll(nodes.flatMap { compiledClass -> compiledClass.fields as Iterable<CompiledField>}.asFlow())
    }

    @InternalCoroutinesApi
    fun methods() : Flow<CompiledMethod> = flow {
        this.emitAll(nodes.flatMap { c -> c.methods as Iterable<CompiledMethod>}.asFlow())
    }

    @InternalCoroutinesApi
    fun fieldRefs() : Flow<FieldReferenceNode> = flow {
        this.emitAll(nodes.flatMap { c -> c.methods.flatMap { node ->
            (node as CompiledMethod).instructions.filterIsInstance(
                FieldReferenceNode::class.java) }
                as Iterable<FieldReferenceNode>}.asFlow())
    }

    @InternalCoroutinesApi
    fun methodRefs() : Flow<MethodReferenceNode> = flow {
        this.emitAll(nodes.flatMap { c -> c.methods.flatMap { node ->
            (node as CompiledMethod).instructions.filterIsInstance(
                MethodReferenceNode::class.java) }
                as Iterable<MethodReferenceNode>}.asFlow())
    }



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
                }
                processor.complete()
            }
        }
    }
}

