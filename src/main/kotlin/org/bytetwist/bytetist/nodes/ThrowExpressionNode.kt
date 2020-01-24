package org.bytetwist.bytetist.nodes

import org.objectweb.asm.tree.AbstractInsnNode

class ThrowExpressionNode(
    val method: CompiledMethod,
    val descriptor: String,
    val instructions :List<AbstractInsnNode>
) : CompiledNode {

    fun delete() {
        instructions.forEach {
            method.instructions.remove(it)
        }
    }
}