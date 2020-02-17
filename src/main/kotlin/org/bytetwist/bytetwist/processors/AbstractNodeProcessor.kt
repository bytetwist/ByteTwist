package org.bytetwist.bytetwist.processors

import com.google.common.base.Stopwatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.bytetwist.bytetwist.nodes.ByteNode
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

internal val log = KotlinLogging.logger { }

@Suppress("UNCHECKED_CAST")
abstract class AbstractNodeProcessor<T : ByteNode> {


    internal val timer = Stopwatch.createUnstarted()

    internal val nodesProcessed = AtomicInteger()

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
     *
     */
    open fun subscribe(flow: Flow<T>) {
        runBlocking {
            flow.collect { accept(it) }
        }
    }

    private fun accept(node: T) {
        if (!timer.isRunning) {
            timer.start()
        }
        if (preProcess(node)) {
            process(node)
            nodesProcessed.getAndIncrement()
        }
    }

    inline fun <reified T : ByteNode> type() = T::class

    abstract val type: KClass<T>


    open fun preProcess(node: T): Boolean {
        return true
    }

    abstract fun process(node: T)

}

fun <T : ByteNode> oneOff(type: KClass<T>, process: (node: T) -> Unit): AbstractNodeProcessor<T> {
    return object : AbstractNodeProcessor<T>() {
        override val type = type

        override fun process(node: T) {
            process(node)
        }
    }
}