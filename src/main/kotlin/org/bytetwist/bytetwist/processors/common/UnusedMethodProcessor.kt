package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.CompiledMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor

class UnusedMethodProcessor : AbstractNodeProcessor<CompiledMethod>() {
    override val type = CompiledMethod::class

    override fun process(node: CompiledMethod) {
        node.delete()
    }

    override fun preProcess(node: CompiledMethod): Boolean {
        return node.invocations.size == 0
    }
}