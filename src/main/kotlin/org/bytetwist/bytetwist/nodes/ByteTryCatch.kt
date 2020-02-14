package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.Type
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.TryCatchBlockNode

/**
 *
 */
class ByteTryCatch(
    val method: ByteMethod,
    start: LabelNode,
    end: LabelNode,
    handler: LabelNode,
    type: String
) : TryCatchBlockNode(
    start,
    end,
    handler,
    type
), ByteNode {


    /**
     *
     */
    fun getType() = Type.getObjectType(type)

    fun tryBlock(): ByteBlockNode {
        if (method.blocks.isNotEmpty() && method.findBlock(start) != null) {
            return method.findBlock(start)!!
        }
        val block = Block(method)
        var included = false
        for ((i, instruction) in method.instructions.withIndex()) {
            val startIndex = method.instructions.indexOf(start)
            val endIndex = method.instructions.indexOf(end)
            if (i !in startIndex..endIndex) {
                continue
            }
            block.add(instruction)
        }
        if (!method.blocks.contains(block)) {
            method.blocks.add(block)
        }
            return block
    }

}