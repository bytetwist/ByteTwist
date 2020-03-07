package org.bytetwist.bytetwist.nodes

import kotlinx.coroutines.flow.flow
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.JumpInsnNode

typealias Block = ByteBlockNode

/**
 * A Basic block: A set of instructions that all exist in the same method and that
 * are executed sequentially without branching. Usually will be enclosed in brackets
 * @param method: The [ByteMethod] that this block/set of instructions belongs to.
 */
class ByteBlockNode(
    val method: ByteMethod
    ) : ByteNode, HashSet<AbstractInsnNode>() {

    init {
    }

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


fun ByteMethod.find(instruction: AbstractInsnNode): Block? {
    return blocks.find { block -> block.contains(instruction) }
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





