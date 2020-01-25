package org.bytetwist.bytetwist.scanners

import kotlinx.coroutines.*
import org.bytetwist.bytetwist.exceptions.NoInputDir
import org.objectweb.asm.ClassReader
import org.bytetwist.bytetwist.nodes.CompiledClass
import org.bytetwist.bytetwist.nodes.CompiledMethod
import org.bytetwist.bytetwist.processors.common.*
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarEntry
import java.util.jar.JarFile

@ExperimentalCoroutinesApi
class DoublePassScanner : Scanner() {

    private val classFiles = CopyOnWriteArrayList<ByteArray>()

    val nodes = processors.nodes

    /**
     * Scans the user submitted input location for any compiled class files
     */
    override fun scan() {
        runBlocking {
            if (inputDir == null) {
                throw NoInputDir()
            }
            inputDir!!.walkTopDown().forEach {
                loadBytesFromFile(it)
            }
            classFiles.forEach {
                val classNode = CompiledClass()
                ClassReader(it).apply {
                    accept(classNode, ClassReader.EXPAND_FRAMES)
                }
                processors.nodes.add(classNode)
            }
            processors.nodes.forEach { clazz ->
                launch {
                    clazz.buildHierarchy()
                    for (mn in clazz.methods) {
                        launch {
                            if (mn is CompiledMethod) {

                                mn.fieldReferences().forEach {
                                    launch {
                                        it.addToField()
                                    }
                                }
                                mn.methodCalls().forEach {
                                    launch {
                                        it.addToMethod()
                                    }
                                }
                                mn.typeReferences().forEach {
                                    launch {
                                        it.addToClass()
                                    }
                                }
                                mn.analyzeBlocks()
                            }
                        }
                    }
                }
            }
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
    scanner.inputDir = File("C:\\Users\\andrea\\IdeaProjects\\bytetwist\\186.jar")
    scanner.addProcessor(AbstractMethodProcessor())
    scanner.addProcessor(UnusedMethodProcessor())
    scanner.addProcessor(UnusedFieldProcessor())
    scanner.addProcessor(FieldRenamer())
    scanner.addProcessor(MethodRenamer())
    scanner.addProcessor(ClassRenamer())
//
    scanner.addProcessor(JarOutputProcessor())
    scanner.scan()
    scanner.run()
}