package org.bytetwist.bytetwist.nodes

import org.bytetwist.bytetwist.References
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.FieldInsnNode

/**
 * An abstraction of a FieldInsnNode that includes references to the ByteField Object and the referencing method
 */
open class FieldReferenceNode(
        val method: ByteMethod,
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String?
) :
    FieldInsnNode(opcode, owner, name, descriptor), ByteNode {

    fun staticReference() = opcode == GETSTATIC || opcode == PUTSTATIC

    /**
     * the field referenced by this instruction
     */
    fun field() = References.fieldNames["$owner.$name"]

    /**
     * Adds this reference to the ByteField object it references
     */
    fun addToField() {
        if (field() == null) {
            val field = References.fieldNames["${method.parent.superClass()?.name}.$name"]
            field?.references?.add(this)
            return
        }
        field()?.references?.add(this)
    }
}

class FieldRead(method: ByteMethod, opcode: Int, owner: String, name: String, descriptor: String?) :
    FieldReferenceNode(method, opcode, owner, name, descriptor),
    ByteNode

class FieldWrite(method: ByteMethod, opcode: Int, owner: String, name: String, descriptor: String?) :
    FieldReferenceNode(method, opcode, owner, name, descriptor),
    ByteNode


object FieldOpcodes {
    val WRITE_CODES = listOf(PUTFIELD, PUTSTATIC, H_PUTFIELD, H_PUTSTATIC)
    val READ_CODES = listOf(GETFIELD, GETSTATIC, H_GETFIELD, H_GETSTATIC)
    val INT_ARITHMATIC = listOf(ISUB, IADD, IMUL, IDIV)

}