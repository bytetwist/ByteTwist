package org.bytetwist.bytetwist.scanners

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.common.*
import org.bytetwist.bytetwist.processors.log
import org.objectweb.asm.ClassReader
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.system.measureTimeMillis

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class DoublePassScanner : Scanner() {

    private val classFiles = CopyOnWriteArrayList<ByteArray>()

    val nodes = processors.nodes



    /**
     * Scans the user submitted input location for any compiled class files
     */
    override fun scan() {
        if (inputDir == null) {
            throw NoInputDir()
        }
        log.info { "Scanning finished in " +
            measureTimeMillis {
                runBlocking {
                    inputDir!!.walkTopDown().forEach {
                        loadBytesFromFile(it)
                    }
                    classFiles.forEach {
                        val classNode = ByteClass()
                        ClassReader(it).apply {
                            accept(classNode, ClassReader.EXPAND_FRAMES)
                        }
                        processors.nodes.add(classNode)
                    }
                    processors.nodes.onEach { clazz ->
                        launch {
                            clazz.buildHierarchy()
                        }
                        launch {
                            for (mn in clazz.methods) {

                                launch {

                                    if (mn is ByteMethod) {
                                        launch {
                                            mn.buildBlocks()
                                        }
                                        launch {
                                            for (feldRef in mn.fieldReferences()) {
                                                launch {
                                                    feldRef.addToField()
                                                }
                                            }
                                        }
                                        launch {
                                            for (methodCalls in mn.methodCalls()) {
                                                launch {
                                                    methodCalls.addToMethod()
                                                }
                                            }
                                        }
                                        launch {
                                            for (typeRefs in mn.typeReferences()) {
                                                launch {
                                                    typeRefs.addToClass()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } / 1000.0 + " ms."
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

    private fun scanJar(f: JarFile) {
        for (jarEntry in f.entries()) {
            if (jarEntry.name.endsWith("class")) {
                classFiles.add(bytes(f, jarEntry))
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


}

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
fun main() {
    val classProcessor =
        BasicClassProcessor()// as AbstractProcessor<*>
    val scanner = DoublePassScanner()
    scanner.inputDir = File("gamepack_obf.jar")
    scanner.addProcessor(FieldRenamer())
    scanner.addProcessor(MethodRenamer())
    scanner.addProcessor(AbstractMethodProcessor())
//    scanner.addProcessor(
//        oneOff(ByteMethod::class) {
//            if (it.name == "method320") {
//                runBlocking {
//                    with(JFrame()) {
//                        add(JTree(it.blockTreeModel))
//                        pack()
//                        isEnabled = true
//                        isVisible = true
//                    }
//                }
//            }
//            else
//                log.info { it.name }
//        })
    scanner.addProcessor(JarOutputProcessor())
    scanner.scan()
    scanner.run()

}