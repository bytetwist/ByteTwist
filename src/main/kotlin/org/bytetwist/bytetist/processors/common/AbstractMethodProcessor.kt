package org.bytetwist.bytetist.processors.common

import org.bytetwist.bytetist.nodes.CompiledMethod
import org.bytetwist.bytetist.processors.AbstractProcessor

class AbstractMethodProcessor : AbstractProcessor<CompiledMethod>() {
    override val type = CompiledMethod::class

    override fun preProcess(node: CompiledMethod): Boolean {
        return node.isAbstract()
    }

    override fun process(node: CompiledMethod) {
        node.delete()
    }
}