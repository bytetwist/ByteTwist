package org.bytetwist.bytetist.scanners

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetist.nodes.CompiledNode
import org.bytetwist.bytetist.processors.AbstractProcessor
import org.bytetwist.bytetist.processors.ProcessingQueue
import java.io.File

abstract class Scanner {

    var inputDir: File? = null

    val processors = ProcessingQueue()

    /**
     * Adds a processor to the Processing Queue
     */
    fun <T : CompiledNode> addProcessor(processor: AbstractProcessor<T>) {
        processors.addProcessor(processor)
    }

    /**
     * Runs all processors from the ProcessingQueue on the scanned nodes
     */
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