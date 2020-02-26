package org.bytetwist.bytetwist.scanners

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bytetwist.bytetwist.Settings
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.log
import org.objectweb.asm.ClassReader
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.system.measureTimeMillis

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
/**
 * A reference building type of scanner that iterates over the scanned bytes twice. The first pass does
 * nothing other than build the pool of scanned classes/methods/etc. Uses coroutines
 */
class DoublePassScanner(inputDir: File) : Scanner(inputDir) {

    private val classFiles = CopyOnWriteArrayList<ByteArray>()

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Scans the user submitted input location for any compiled class files
     */
    override fun scan(): Flow<ByteClass> {

        return runBlocking(dispatcher) {


            return@runBlocking channelFlow {
                log.info {
                    "Scanning finished in " +
                            measureTimeMillis {
                                runBlocking {
                                inputDir.walkTopDown().forEach {
                                    launch {

                                        loadBytesFromFile(it)
                                    }
                                    }
                                }
                                classFiles.forEach { clazz ->
                                    val byteClass = fromBytes(clazz)
                                    launch {
                                        byteClass.buildHierarchy()
                                    }
                                    launch {
                                        this.analyzeMethods(byteClass)
                                    }
                                    launch {
                                        send(byteClass)
                                    }
                                }

                            }    / 1000.0 + " seconds."
                }

        }
        }
    }

    /**
     * Builds the set of [org.bytetwist.bytetwist.nodes.ByteBlockNode]s from each method in [clazz].
     * Also takes all of the [org.bytetwist.bytetwist.nodes.FieldReferenceNode]s in the method and adds them
     * to the [org.bytetwist.bytetwist.nodes.FieldReferenceNode] if the field was scanned by the scanner.
     * Does the same with [org.bytetwist.bytetwist.nodes.MethodReferenceNode]s and the
     * [org.bytetwist.bytetwist.nodes.ClassReferenceNode]
     */
    private fun CoroutineScope.analyzeMethods(clazz: ByteClass) {
        for (mn in clazz.methods) {
            launch {
                if (mn is ByteMethod) {
                    awaitAll(
                    async (Dispatchers.IO) {
                            buildBlocks(mn)
                        },
                    async {
                        buildFieldReferences(mn)
                    },
                    async {
                        buildMethodCalls(mn)
                    },
                    async {
                        buildClassRefs(mn)
                    })
                }
            }
        }
    }


/**
 * Iterates over all of the [org.bytetwist.bytetwist.nodes.ClassReferenceNode] in the
 * [org.bytetwist.bytetwist.nodes.ByteMethod] [mn] and adds them to the [ByteClass]
 */
private fun CoroutineScope.buildClassRefs(mn: ByteMethod) {
    for (typeRefs in mn.typeReferences()) {
        launch {
            typeRefs.addToClass()
        }
    }
}

/**
 * Iterates over all of the [org.bytetwist.bytetwist.nodes.MethodReferenceNode] in the
 * [org.bytetwist.bytetwist.nodes.ByteMethod] [mn] and adds them to the [ByteMethod]
 */
private fun CoroutineScope.buildMethodCalls(mn: ByteMethod) {
    for (methodCalls in mn.methodCalls()) {
        launch {
            methodCalls.addToMethod()
        }
    }
}

/**
 * Iterates over all of the [org.bytetwist.bytetwist.nodes.FieldReferenceNode] in the
 * [org.bytetwist.bytetwist.nodes.ByteMethod] [mn] and adds them to the [org.bytetwist.bytetwist.nodes.ByteField]
 */
private suspend fun buildFieldReferences(mn: ByteMethod) {
    withContext(dispatcher) {
        for (fieldRef in mn.fieldReferences()) {
            launch {
                fieldRef.addToField()
            }
        }
    }
}

/**
 * Constructs all of the [org.bytetwist.bytetwist.nodes.Block] found in [ByteMethod] [mn].
 * Adds them to the [ByteMethod].
 * This might be expensive depending on the size of the method
 */
private fun CoroutineScope.buildBlocks(mn: ByteMethod) {
    launch {
        mn.buildBlocks()
    }
}


/**
 * Takes either a .class file or a .jar file and loads the ByteArray into the List of class Files
 * @param it - The Jar or class file to load
 */
private fun loadBytesFromFile(it: File) {
    if (it.extension.toLowerCase() == "jar") {
        scanJar(JarFile(it))
    }
    if (it.extension.toLowerCase() == "class") {
        classFiles.add(it.readBytes())
    }
}

/**
 * Scans a [JarFile] and adds the bytes from all of the class file [JarEntry]s to [classFiles]
 */
private fun scanJar(f: JarFile) {
    for (jarEntry in f.entries()) {
        runBlocking(dispatcher) {
            launch {
                if (jarEntry.name.endsWith("class")) {
                    classFiles.add(bytes(f, jarEntry))
                }
            }
        }
    }
}

/**
 * Returns a ByteArray from a JarEntry of a JarFile
 * @param f - The JarFile containing the entry
 * @param jarEntry - the JarEntry to get bytes from
 * @return a ByteArray of the class file
 */
private fun bytes(f: JarFile, jarEntry: JarEntry?) =
    f.getInputStream(jarEntry).readBytes()

    companion object {
        val dispatcher =
            Executors.newFixedThreadPool(Settings.scannerThreads).asCoroutineDispatcher()
    }
}

/**
 * Creates a [ByteClass] from a [ByteArray] using a [ClassReader]
 */
fun fromBytes(byteArray: ByteArray): ByteClass {
    val clazz = ByteClass()
    ClassReader(byteArray).accept(clazz, ClassReader.EXPAND_FRAMES)
    return clazz
}



