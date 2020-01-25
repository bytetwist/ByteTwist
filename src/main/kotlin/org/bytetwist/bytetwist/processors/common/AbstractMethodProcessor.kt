package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.CompiledMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor

class AbstractMethodProcessor : AbstractNodeProcessor<CompiledMethod>() {
    override val type = CompiledMethod::class

    override fun preProcess(node: CompiledMethod): Boolean {
        return node.isAbstract()
    }

    override fun process(node: CompiledMethod) {
        node.delete()
    }
}