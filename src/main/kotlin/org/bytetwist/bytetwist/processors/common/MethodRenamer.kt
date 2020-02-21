package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class MethodRenamer : AbstractNodeProcessor<ByteMethod>() {
    override val type: KClass<ByteMethod>
        get() = ByteMethod::class

    override fun process(node: ByteMethod) {
        node.annotate("Renamed", "oldName" to node.name)
        runBlocking {
            node.rename("method${nodesProcessed}")
        }
    }

    override fun shouldProcess(node: ByteMethod): Boolean {
        return node.name.length <= 3 || node.name.length > 60
    }
}