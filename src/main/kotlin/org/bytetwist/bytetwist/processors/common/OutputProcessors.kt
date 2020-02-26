package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class JarOutputProcessor(outputName: String = "out") : AbstractNodeProcessor<ByteClass>() {

    private val fileOutputStream = FileOutputStream("$outputName.jar")
    private val jarOutputStream = JarOutputStream(fileOutputStream)

    override fun process(node: ByteClass) {
        runBlocking {
            addClassToJar(node)
        }
    }

    private fun addClassToJar(node: ByteClass) {
            jarOutputStream.putNextEntry(ZipEntry(node.name + ".class"))
            jarOutputStream.write(node.toBytes())
            jarOutputStream.closeEntry()
    }

    override fun onComplete() {
        jarOutputStream.finish()
        super.onComplete()
    }

    override val type: KClass<ByteClass>
        get() = ByteClass::class
}

class ClassOutputProcessor(outputDir: String = "out") : AbstractNodeProcessor<ByteClass>() {

    private val dir = File(outputDir)

    init {
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw IOException("Unable to create output dir for ClassOutputProcessor")
            }
        }
    }

    /**
     * The [KClass] type of node that this processor will process. Annoying but couldn't figure out a better way
     */
    override val type = ByteClass::class

    /**
     * Processes each object that is accepted and passes [shouldProcess]. This is where the main code for
     * analysis/transformation, etc should go.
     */
    override fun process(node: ByteClass) {
        val file = File(dir, node.name + ".class")
        file.createNewFile()
        val outputStream = FileOutputStream(file, false)
        outputStream.write(node.toBytes())
        outputStream.close()
    }

}
