package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.tree.MethodInsnNode
import org.bytetwist.bytetwist.References

/**
 * An Abstraction of a MethodInsnNode that includes a reference to the method it calls
 */
class MethodReferenceNode(
        val calledFrom: ByteMethod,
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
) :
    MethodInsnNode(opcode, owner, name, descriptor, isInterface),
    ByteNode {

    fun getMethod() : ByteMethod? = References.methodNames["$owner.$name.$desc"]

    fun addToMethod() {
        getMethod()?.invocations?.add(this)
    }
}