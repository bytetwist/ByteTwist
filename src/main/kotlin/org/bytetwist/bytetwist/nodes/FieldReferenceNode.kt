package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.bytetwist.bytetwist.References
import org.objectweb.asm.Opcodes.*

/**
 * An abstraction of a FieldInsnNode that includes references to the CompiledField Object and the referencing method
 */
open class FieldReferenceNode(
    val method: CompiledMethod,
    opcode: Int,
    owner: String,
    name: String,
    descriptor: String?
) :
    FieldInsnNode(opcode, owner, name, descriptor), CompiledNode {

    fun staticReference() = opcode == GETSTATIC || opcode == PUTSTATIC

    /**
     * the field referenced by this instruction
     */
    fun field() = References.fieldNames["$owner.$name"]

    /**
     * Adds this reference to the CompiledField object it references
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

class FieldRead(method: CompiledMethod, opcode: Int, owner: String, name: String, descriptor: String?) :
    FieldReferenceNode(method, opcode, owner, name, descriptor),
    CompiledNode

class FieldWrite(method: CompiledMethod, opcode: Int, owner: String, name: String, descriptor: String?) :
    FieldReferenceNode(method, opcode, owner, name, descriptor),
    CompiledNode


object FieldOpcodes {
    val WRITE_CODES = listOf(PUTFIELD, PUTSTATIC, H_PUTFIELD, H_PUTSTATIC)
    val READ_CODES = listOf(GETFIELD, GETSTATIC, H_GETFIELD, H_GETSTATIC)
    val INT_ARITHMATIC = listOf(ISUB, IADD, IMUL, IDIV)

}