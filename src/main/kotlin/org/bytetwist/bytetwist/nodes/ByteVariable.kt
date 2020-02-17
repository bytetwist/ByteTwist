package org.bytetwist.bytetwist.nodes

import com.google.common.collect.Range
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.VarInsnNode

import java.util.concurrent.CopyOnWriteArraySet

class ByteVariable(
    method: ByteMethod,
    name: String,
    descriptor: String,
    signature: String?,
    start: LabelNode?,
    end: LabelNode?,
    index: Int
) :
    LocalVariableNode(
        name, descriptor, signature,
        start,
        end,
        index
    ), ByteNode {

    val references = CopyOnWriteArraySet<ByteVariableReference>()

    val reads = references.filter { it.opcode in Range.open(21, 53) }

    val writes = references.filter { it.opcode in Range.open(54, 86) }



}

class ByteVariableReference(opcode: Int, variable: Int) : VarInsnNode(opcode, variable), ByteNode