package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.FieldReferenceNode
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor

@ExperimentalCoroutinesApi
class FieldReferenceProcessor : AbstractNodeProcessor<FieldReferenceNode>() {
    override val type= FieldReferenceNode::class

    override fun process(node: FieldReferenceNode) {

    }

    override fun preProcess(node: FieldReferenceNode): Boolean {
        return node.field() != null// && node.opcode != Opcodes.INVOKEVIRTUAL
    }
}