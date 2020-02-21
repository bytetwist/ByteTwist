package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import java.io.FileOutputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class JarOutputProcessor(private val outputName: String = "out") : AbstractNodeProcessor<ByteClass>() {

    private val fileOutputStream = FileOutputStream("$outputName.jar")
    private val jarOutputStream = JarOutputStream(fileOutputStream)

    init {

    }

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
