package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor

class AbstractMethodProcessor : AbstractNodeProcessor<ByteMethod>() {
    override val type = ByteMethod::class

    override fun preProcess(node: ByteMethod): Boolean {
        return node.isAbstract()
    }

    override fun process(node: ByteMethod) {
        node.delete()
    }
}