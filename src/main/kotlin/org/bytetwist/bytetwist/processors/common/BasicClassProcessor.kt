package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.log


@ExperimentalCoroutinesApi
class BasicClassProcessor : AbstractNodeProcessor<ByteClass>() {
     override val type = ByteClass::class

    override fun preProcess(node: ByteClass): Boolean {
        return node.methods.filterIsInstance(ByteMethod::class.java).all { methodNode ->
            methodNode.isStatic() } &&
                node.fields.all { fieldNode -> (fieldNode as ByteField).isStatic() }
    }

    override fun process(node: ByteClass) {
        log.info { "Processing ${node.name}" }
    }
}