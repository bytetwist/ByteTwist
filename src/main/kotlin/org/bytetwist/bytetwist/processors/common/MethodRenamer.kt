package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.CompiledMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class MethodRenamer : AbstractNodeProcessor<CompiledMethod>() {
    override val type: KClass<CompiledMethod>
        get() = CompiledMethod::class

    override fun process(node: CompiledMethod) {
        node.rename("method${nodesProcessed}")
    }

    override fun preProcess(node: CompiledMethod): Boolean {
        return node.name.length <= 3 || node.name.length > 60
    }
}