package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.JumpInsnNode

class CompiledBlockNode(
    val method: CompiledMethod
) : HashSet<AbstractInsnNode>(), CompiledNode {

    val edges = HashSet<Pair<JumpInsnNode, CompiledBlockNode>>()

}

fun InsnList.buildBlock(method: CompiledMethod, startIndex: Int) : CompiledBlockNode {
    var i = startIndex
    val block = CompiledBlockNode(method)
    while (this[i] !is JumpInsnNode) {
        block.add(this[i])
        if (i == this.size() - 1) {
            return block
        }
        i++
    }
    block.add(this[i])
    return block
}

