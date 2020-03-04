package org.bytetwist.bytetwist

import com.google.common.annotations.VisibleForTesting
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.bytetwist.bytetwist.exceptions.UninitializedScanner
import org.bytetwist.bytetwist.nodes.*
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.ProcessingQueue
import org.bytetwist.bytetwist.processors.common.*
import org.bytetwist.bytetwist.processors.log
import org.bytetwist.bytetwist.processors.oneOff
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import java.io.File
import javax.imageio.ImageIO

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
        runBlocking {
            processors.process()
            DoublePassScanner.dispatcher.close()
        }
    }

    /**
     * Internal method that starts the scanner or throws an exception if the scanner isn't initialized
     */
    @VisibleForTesting
    suspend fun runScanner() {
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
    loader.scan("gamepack_obf.jar")
    loader.addProcessor(ClassRenamer())
//    loader.addProcessor(UnusedMethodProcessor())
//    loader.addProcessor(UnusedFieldProcessor())
    loader.addProcessor(MethodRenamer())
    loader.addProcessor(FieldRenamer())
     loader.addProcessor(oneOff<ByteMethod> {
         log.info { it.drawFlowGraph() }
//         if (it.drawFlowGraph() != null) {
//             ImageIO.write(it.drawFlowGraph(), "png", File("graphs", "${it.name}.png"))
//         }
         })
    loader.addProcessor(JarOutputProcessor())
    loader.launch()

}