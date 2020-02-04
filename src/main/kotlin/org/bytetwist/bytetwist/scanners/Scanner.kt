package org.bytetwist.bytetwist.scanners

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.nodes.CompiledNode
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.ProcessingQueue
import java.io.File

@InternalCoroutinesApi
abstract class Scanner : CoroutineScope {

    var inputDir: File? = null
    lateinit var processors: ProcessingQueue

    /**
     * Adds a processor to the Processing Queue
     */
    fun <T : CompiledNode> addProcessor(processor: AbstractNodeProcessor<T>) {
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