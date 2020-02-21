package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import kotlin.reflect.KClass

class UnusedMethodProcessor(override val type: KClass<ByteMethod> = ByteMethod::class) : AbstractNodeProcessor<ByteMethod>() {

    override fun process(node: ByteMethod) {
        node.delete()
    }

    override fun shouldProcess(node: ByteMethod): Boolean {
        return node.invocations.size == 0
    }
}