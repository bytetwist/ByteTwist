package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.nodes.CompiledClass
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import java.io.FileOutputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class JarOutputProcessor : AbstractNodeProcessor<CompiledClass>() {



    override fun process(node: CompiledClass) {
        runBlocking {
            addClassToJar(node)
        }
    }

    private fun addClassToJar(node: CompiledClass) {
            jarOutputStream.putNextEntry(ZipEntry(node.name + ".class"))
            jarOutputStream.write(node.toBytes())
            jarOutputStream.closeEntry()

    }

    override fun onComplete() {
        jarOutputStream.finish()
        super.onComplete()
    }

    companion object {
        private val fileOutputStream = FileOutputStream("out.jar")
        val jarOutputStream = JarOutputStream(fileOutputStream)
    }

    override val type: KClass<CompiledClass>
        get() = CompiledClass::class
}