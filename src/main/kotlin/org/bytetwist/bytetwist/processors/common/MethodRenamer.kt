package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class MethodRenamer : AbstractNodeProcessor<ByteMethod>() {
    override val type: KClass<ByteMethod>
        get() = ByteMethod::class

    override fun process(node: ByteMethod) {
        node.annotate("Renamed", "oldName" to node.name)
        node.rename("method${nodesProcessed}")
    }

    override fun preProcess(node: ByteMethod): Boolean {
        return node.name.length <= 3 || node.name.length > 60
    }
}