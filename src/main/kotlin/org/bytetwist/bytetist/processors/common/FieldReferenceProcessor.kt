package org.bytetwist.bytetist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetist.nodes.FieldReferenceNode
import org.bytetwist.bytetist.processors.AbstractProcessor

@ExperimentalCoroutinesApi
class FieldReferenceProcessor : AbstractProcessor<FieldReferenceNode>() {
    override val type= FieldReferenceNode::class

    override fun process(node: FieldReferenceNode) {

    }

    override fun preProcess(node: FieldReferenceNode): Boolean {
        return node.field() != null// && node.opcode != Opcodes.INVOKEVIRTUAL
    }
}