package org.bytetwist.bytetwist.scanners

import kotlinx.coroutines.*
import org.objectweb.asm.ClassReader
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.ProcessingQueue
import java.io.File
import java.util.jar.JarFile

@InternalCoroutinesApi
@Deprecated("Use DoublePassScanner")
class SinglePassScanner : Scanner() {


    /**
     * Scans the user submitted input location for any compiled class files
     */
    override fun scan() = runBlocking {
        if (inputDir?.isDirectory!!) {
            scanDir()
        } else {
            if (inputDir!!.extension.equals("jar", true)) {
                launch {
                    loadJar(toJarFile(inputDir!!))
                }.start()
            }
            if (inputDir!!.extension.equals("class", true)) {
                launch {
                    loadClass(fileBytes(inputDir!!))
                }.start()
            }
        }
        ProcessingQueue.nodes.forEach {
            launch {
                scanFields(it)
            }
            launch {
                scanMethods(it)
            }
        }
    }

    /**
     * Scans a directory Top Down and find and loads any class files found
     */
    private fun scanDir() {
        GlobalScope.async {
            inputDir!!.walkTopDown().takeWhile { file ->
                file.name.endsWith(".class", true) ||
                        file.name.endsWith(".jar")
            }.forEach { f ->
                if (f.name.endsWith(".jar", true)) {
                    launch {
                        loadJar(toJarFile(f))
                    }
                }
                if (f.name.endsWith(".class", true)) {
                    launch {
                        loadClass(fileBytes(f))
                    }
                }
            }
        }
    }


    /**
     * Loads all the .class files from a JarFile into the list of the nodes to be processed
     * @param f - The JarFile to load classes from
     */
    private fun loadJar(f: JarFile) {
        with(f) {
            this.entries().iterator().forEach { jarEntry ->
                if (jarEntry.name.endsWith("class")) {
                    loadClass(f.getInputStream(jarEntry).readBytes())
                }
            }
        }
    }

    /**
     * Loads a ByteArray into the ProcessingQueue's list of nodes to be processed
     * @param bytes - The ByteArray of the node
     */
    private fun loadClass(bytes: ByteArray) {
        val node = readClass(bytes)
        ProcessingQueue.nodes.add(node)
    }

    /**
     * Gets a ByteArray from a file
     * @param f - the File
     * @return a ByteArray from the file
     */
    private fun fileBytes(f: File) = f.inputStream().readBytes()

    /**
     * Reads a Compiled Class from a ByteArray
     * @param b - The ByteArray
     * @return The ByteClass object that represents the bytes
     */
    private fun readClass(b: ByteArray): ByteClass {
        val node = ByteClass()
        ClassReader(b).apply {
            accept(node, ClassReader.SKIP_DEBUG)
        }
        return node
    }

    /**
     * Used for getting a JarFile object from a File object
     * @param f - the file of a .jar
     * @return a JarFile from f
     */
    private fun toJarFile(f: File): JarFile {
        return JarFile(f)
    }

    private fun scanFields(classNode: ByteClass) {
        classNode.fields.forEach {
            if (it is ByteField) {
                processors.fieldNodes.add(it)
            }
        }
    }

    private fun scanMethods(classNode: ByteClass) {
        classNode.methods.forEach {
            if (it is ByteMethod) {
                processors.methodNodes.add(it)
            }
        }
    }
}


