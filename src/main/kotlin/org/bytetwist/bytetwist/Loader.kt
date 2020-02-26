package org.bytetwist.bytetwist

import com.google.common.base.Stopwatch
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.bytetwist.bytetwist.exceptions.UninitializedScanner
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.nodes.ByteNode
import org.bytetwist.bytetwist.nodes.ByteConstructor
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.ProcessingQueue
import org.bytetwist.bytetwist.processors.common.*
import org.bytetwist.bytetwist.processors.log
import org.bytetwist.bytetwist.processors.oneOff
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.objectweb.asm.Type
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.objectweb.asm.tree.analysis.BasicValue
import java.io.File
import kotlin.system.measureTimeMillis

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

        val timer = Stopwatch.createStarted()
        val analyzer = Analyzer<BasicValue>(BasicInterpreter())
        loader.scan("C:\\Users\\Jesse\\IdeaProjects\\ByteTwist\\gamepack_obf.jar")
        Settings.annotateClassChanges = true
        loader.addProcessor(AbstractMethodProcessor())
        loader.addProcessor(ClassRenamer())
        loader.addProcessor(MethodRenamer())
        loader.addProcessor(FieldRenamer())
        loader.addProcessor(oneOff<ByteMethod> { it.printCfgDot() })
        loader.addProcessor(StaticFieldDeconstructor())
        loader.addProcessor(JarOutputProcessor())
        loader.launch()
        println("ByteTwist finished in ${timer.elapsed().toMillis() / 1000.0} seconds")
    }
}

