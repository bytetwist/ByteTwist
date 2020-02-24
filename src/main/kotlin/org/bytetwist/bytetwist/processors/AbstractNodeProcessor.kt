package org.bytetwist.bytetwist.processors

import com.google.common.base.Stopwatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.nodes.ByteNode
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

internal val log = KotlinLogging.logger { }

@Suppress("UNCHECKED_CAST")
/**
 * Abstract class for a processor that will accept all instances of its type for processing.
 */
abstract class AbstractNodeProcessor<T : ByteNode> {

    internal val timer = Stopwatch.createUnstarted()

    internal val nodesProcessed = AtomicInteger()

    /**
     *  Calls both the internal and the overridable completion methods
     */
    fun complete() {
        onComplete()
        finished()
    }

    /**
     * This method gets called after the processor has finished processing all of its nodes.
     * It can be overridden to perform any post-processing logic
     */
    open fun onComplete() {
        log.info {
            "${this::class.simpleName ?: "OneOffProcessor".split(".").last()} processed $nodesProcessed nodes in $timer"
        }
    }

    /**
     * Makes sure the Receiver gets shut down so the program doesn't run indefinitely.
     */
    private fun finished() {
//        receiver.cancel()
    }

    /**
     * Subscribes this processor to the flow of [T]. Calls collect on the flow and attempts to process each object
     */
    open fun subscribe(flow: Flow<T>) {
        runBlocking {
            flow.collect { accept(it) }
        }
    }

    /**
     * Tests each node by passing it to [shouldProcess], then processes it if it should be processed.
     * Also starts this processors timer and increments the value that holds the number of nodes this processor
     * has processed
     */
    private fun accept(node: T) {
        if (!timer.isRunning) {
            timer.start()
        }
        if (shouldProcess(node)) {
            process(node)
            nodesProcessed.getAndIncrement()
        }
    }

    /**
     * The [KClass] type of node that this processor will process. Annoying but couldn't figure out a better way
     */
    inline fun <reified T : ByteNode> type() = T::class

    /**
     * The [KClass] type of node that this processor will process. Annoying but couldn't figure out a better way
     */
    abstract val type: KClass<T>

    /**
    * Determines if this processor should process a node. If not overridden, the processor will process all
    * scanned objects of type [T]
    */
    open fun shouldProcess(node: T): Boolean {
        return true
    }

    /**
     * Processes each object that is accepted and passes [shouldProcess]. This is where the main code for
     * analysis/transformation, etc should go.
     */
    abstract fun process(node: T)

}

/**
 * A oneOff processor is simply a method to easily and quickly create a processor on the fly. For example:
 * `        oneOff<ByteMethod> { println(it.name) } ` is the code for a processor that accepts all scanned [ByteMethod]s
 * and prints the name of each. A oneOff processor can be added to the [ProcessingQueue] the same way as any other processor
 * and it will get executed in the order that it is added. This default implementation doesn't override [AbstractNodeProcessor.shouldProcess]
 * or [AbstractNodeProcessor.onComplete] so it is best used in a situation where a simple processor is needed that probably won't be used that often
 */
inline fun <reified T : ByteNode> oneOff(crossinline process: (node: T) -> Unit): AbstractNodeProcessor<T> {
    return object : AbstractNodeProcessor<T>() {

        override fun process(node: T) {
            process(node)
        }

        /**
         * The [KClass] type of node that this processor will process. Annoying but couldn't figure out a better way
         */
        override val type = T::class


    }
}