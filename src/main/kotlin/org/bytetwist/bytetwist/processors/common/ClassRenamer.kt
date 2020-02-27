package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.log
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class ClassRenamer : AbstractNodeProcessor<ByteClass>() {

    override val type: KClass<ByteClass> = ByteClass::class


    override fun shouldProcess(node: ByteClass): Boolean {
        return node.name.length <= 3 || node.name.length > 60
    }

    @InternalCoroutinesApi
    override fun process(node: ByteClass) {
        runBlocking {
            node.rename("Class${nodesProcessed}")
        }
    }
}