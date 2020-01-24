package org.bytetwist.bytetist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetist.nodes.CompiledMethod
import org.bytetwist.bytetist.processors.AbstractProcessor
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class MethodRenamer : AbstractProcessor<CompiledMethod>() {
    override val type: KClass<CompiledMethod>
        get() = CompiledMethod::class

    override fun process(node: CompiledMethod) {
        node.rename("method${nodesProcessed}")
    }
}