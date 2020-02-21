package org.bytetwist.bytetwist

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.bytetwist.bytetwist.exceptions.UninitializedScanner
import org.bytetwist.bytetwist.nodes.ByteNode
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.ProcessingQueue
import org.bytetwist.bytetwist.processors.common.AbstractMethodProcessor
import org.bytetwist.bytetwist.processors.common.ClassRenamer
import org.bytetwist.bytetwist.processors.common.JarOutputProcessor
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import java.io.File

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
/**
 * This class is the main entrance point for the API. It is used to link a [DoublePassScanner] and a
 * [ProcessingQueue] to run all of the processors added to the queue on the scanned classes.
 */
open class Loader {

    val processors = ProcessingQueue()

    private var scanner: DoublePassScanner? = null

    /**
     * Initializes the [DoublePassScanner] and starts the scanning process
     */
    fun scan(input: File) {
        scanner = DoublePassScanner(input)
        runBlocking {
            runScanner()
        }
    }

    /**
     * Initializes the [DoublePassScanner] and starts the scanning process
     */
    fun scan(input: String) {
        val file = File(input)
        if (!file.exists()) {
            throw NoInputDir()
        }
        scanner = DoublePassScanner(file)
        runBlocking {
            runScanner()
        }
    }

    /**
     * Adds a [AbstractNodeProcessor] to the queue of processors. This must be called before the loader starts
     * the processing via the [launch] method or the processors won't receive any types of node to process.
     */
    fun <T : ByteNode> addProcessor(processor: AbstractNodeProcessor<in T>) {
        processors.addProcessor(processor)
    }

    /**
     * Starts the processing.
     */
    fun launch() {
        processors.process()
    }

    /**
     * Internal method that starts the scanner or throws an exception if the scanner isn't initialized
     */
    private suspend fun runScanner() {
        if (scanner == null) {
             throw UninitializedScanner()
        }
        val scan = scanner!!.scan()
        scan.collect { processors.nodes.add(it) }
    }
}

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
fun main() {
    val loader = Loader()

runBlocking {
    loader.scan("C:\\Users\\Jesse\\IdeaProjects\\ByteTwist\\gamepack_obf.jar")
}
    loader.addProcessor(AbstractMethodProcessor())

    loader.addProcessor(ClassRenamer())
    loader.addProcessor(JarOutputProcessor())

    loader.launch()


}
