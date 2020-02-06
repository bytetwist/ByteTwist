package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor

class UnusedMethodProcessor : AbstractNodeProcessor<ByteMethod>() {
    override val type = ByteMethod::class

    override fun process(node: ByteMethod) {
        node.delete()
    }

    override fun preProcess(node: ByteMethod): Boolean {
        return node.invocations.size == 0
    }
}