package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import java.util.concurrent.CopyOnWriteArrayList

typealias Block = ByteBlockNode

open class ByteBlockNode(
    val method: ByteMethod
) : CopyOnWriteArrayList<AbstractInsnNode>(), ByteNode {

    /**
     * Returns a string representation of this list.  The string
     * representation consists of the string representations of the list's
     * elements in the order they are returned by its iterator, enclosed in
     * square brackets (`"[]"`).  Adjacent elements are separated by
     * the characters `", "` (comma and space).  Elements are
     * converted to strings as by [String::valueOf].
     *
     * @return a string representation of this list
     */
    override fun toString(): String {
        return "Node${method.blocks.indexOf(this)}"
    }

    fun print() = toString() + super.toArray().toString() + "Edges[${this.edges}]"

    public val edges = ArrayList<Pair<ByteBlockNode, EdgeDirection>>()

    public val successors = HashSet<Block>()

    public val predecessors = HashSet<Block>()

    companion object {
        val UNCONDITIONAL_JUMP = listOf(Opcodes.GOTO, Opcodes.JSR, Opcodes.RET)
        val CONDITIONAL_JUMP = listOf(
            Opcodes.IFEQ, Opcodes.IFLT, Opcodes.IFLE, Opcodes.IFNE, Opcodes.IFGT,
            Opcodes.IFGE, Opcodes.IFNULL, Opcodes.IFNONNULL, Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE, Opcodes.IF_ICMPLT,
            Opcodes.IF_ICMPGT, Opcodes.IF_ICMPLE, Opcodes.IF_ICMPGE, Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE, Opcodes.LCMP,
            Opcodes.FCMPL, Opcodes.FCMPG, Opcodes.DCMPL, Opcodes.DCMPG
        )
    }

}

/**
 * A bit redundant but can be useful for analysis
 */
enum class EdgeDirection {
    IN,
    OUT
}

/**
 * Finds a block by it's first [AbstractInsnNode] in a method that has already computed all of its blocks.
 * @param firstInstruction - The first instruction in the block you are trying to get
 */
fun ByteMethod.findBlock(firstInstruction: AbstractInsnNode) =
    blocks.find { block -> block.first() == firstInstruction }

/**
 * Finds a block by it's last [AbstractInsnNode] in a method that has already computed all of its blocks.
 * @param lastInstruction - The last instruction in the block you are trying to get
 */
fun ByteMethod.findBlockByLast(lastInstruction: AbstractInsnNode) =
    blocks.find { block -> block.last() == lastInstruction }





