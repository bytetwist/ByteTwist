package org.bytetwist.bytetist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetist.nodes.CompiledMethod
import org.bytetwist.bytetist.processors.AbstractProcessor

class UnusedMethodProcessor : AbstractProcessor<CompiledMethod>() {
    override val type = CompiledMethod::class

    override fun process(node: CompiledMethod) {
        node.delete()
    }

    override fun preProcess(node: CompiledMethod): Boolean {
        return node.invocations.size == 0
    }
}