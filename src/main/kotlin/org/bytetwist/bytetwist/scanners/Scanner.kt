package org.bytetwist.bytetwist.scanners

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.nodes.ByteNode
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.ProcessingQueue
import java.io.File

@InternalCoroutinesApi
abstract class Scanner {

    var inputDir: File? = null

    val processors = ProcessingQueue()

    /**
     * Adds a processor to the Processing Queue
     */
    fun <T : ByteNode> addProcessor(processor: AbstractNodeProcessor<T>) {
        processors.addProcessor(processor)
    }

    /**
     * Runs all processors from the ProcessingQueue on the scanned nodes
     */
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun run() {
        runBlocking {
            processors.process()
        }
    }

    /**
     * Scans the user submitted input location for any compiled class files
     */
    abstract fun scan()

}