package org.bytetwist.bytetwist.nodes

import com.google.common.collect.HashMultimap
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode

typealias Block = CompiledBlockNode
class CompiledBlockNode(
    val method: CompiledMethod
) : CopyOnWriteArrayList<AbstractInsnNode>(), CompiledNode {

    /**
     * Returns a string representation of this list.  The string
     * representation consists of the string representations of the list's
     * elements in the order they are returned by its iterator, enclosed in
     * square brackets (`"[]"`).  Adjacent elements are separated by
     * the characters `", "` (comma and space).  Elements are
     * converted to strings as by [String.valueOf].
     *
     * @return a string representation of this list
     */
    override fun toString(): String {
        return "Insn ${method.instructions.indexOf(first())}: ${first()} - ${method.instructions.indexOf(last())}: ${last()}"
    }

    public val edges = ArrayList<CompiledBlockNode>()

    public val successors = HashSet<Block>()

    public val predecessors = HashSet<Block>()


    companion object {
        val UNCONDITIONAL_JUMP = listOf(Opcodes.GOTO, Opcodes.JSR, Opcodes.RET)
        val CONDITIONAL_JUMP = listOf(Opcodes.IFEQ, Opcodes.IFLT, Opcodes.IFLE, Opcodes.IFNE, Opcodes.IFGT,
            Opcodes.IFGE, Opcodes.IFNULL, Opcodes.IFNONNULL, Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE, Opcodes.IF_ICMPLT,
            Opcodes.IF_ICMPGT, Opcodes.IF_ICMPLE, Opcodes.IF_ICMPGE, Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE, Opcodes.LCMP,
            Opcodes.FCMPL, Opcodes.FCMPG, Opcodes.DCMPL, Opcodes.DCMPG)


    }


}

class Edge(block: CompiledBlockNode, label: LabelNode) : LabelNode(label.label) {



    var from: Block? = null

}
enum class EdgeDirection {
    IN,
    OUT
}


fun InsnList.buildBlock(method: CompiledMethod, startIndex: Int) : CompiledBlockNode {

    var i = startIndex
    val block = CompiledBlockNode(method)
    while (this[i] !is JumpInsnNode) {
        block.add(this[i])
        if (i == this.size()) {
            return block
        }
        i++
    }
 //   block.add(this[i])
    //method.cfg.putEdgeValue(block, method.blocks.find { compiledBlockNode -> compiledBlockNode.first() == (this[i] as JumpInsnNode).label }, this[i])
    return block
}

