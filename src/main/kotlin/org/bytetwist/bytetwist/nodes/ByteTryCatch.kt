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
    handler: LabelNode?,
    type: String?
) : TryCatchBlockNode(
    start,
    end,
    handler,
    type
), ByteNode {


    /**
     *
     */
    fun getType(): Type = Type.getObjectType(type)

    fun buildMethodBlocks() = tryBlock() to catchBlock()

    /**
     *
     */
    fun tryBlock(): ByteBlockNode? {
        return method.find(start)
//        if (method.blocks.isNotEmpty() && method.findBlock(start) != null) {
//            return method.findBlock(start)!!
//        }
//        val block = Block(method)
//        for ((i, instruction) in method.instructions.withIndex()) {
//            val startIndex = method.instructions.indexOf(start)
//            val endIndex = method.instructions.indexOf(end)
//            if (i !in startIndex..endIndex) {
//                continue
//            }
//            block.add(instruction)
//        }
//        if (!method.blocks.contains(block)) {
//            method.blocks.add(block)
//        }
//            return block
    }

    /**
     * The block that catches any exceptions
     */
    fun catchBlock(): ByteBlockNode? {
        return method.find(handler)
//        if (method.blocks.isNotEmpty() && method.findBlock(handler) != null) {
////            if (!this.tryBlock().edges.contains(method.findBlock(handler) to EdgeDirection.OUT)) {
////                this.tryBlock().edges.add(((method.findBlock(handler) as ByteBlockNode to EdgeDirection.OUT)!!))
////            }
//            return method.findBlock(handler)!!
//        }
//        val block = Block(method)
//        method.instructions.filter { abstractInsnNode ->
//            method.instructions.indexOf(abstractInsnNode) >= method.instructions.indexOf(handler)
//        }
//            .forEach { insnNode ->
//                if (insnNode !is JumpInsnNode) {
//                    block.add(insnNode)
//                }
//            }
//        method.blocks.add(block)
//        return block
    }

}