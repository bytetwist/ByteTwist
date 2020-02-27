package org.bytetwist.bytetwist.nodes

import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.findClass
import org.bytetwist.bytetwist.processors.log
import org.objectweb.asm.Type
import org.objectweb.asm.tree.TypeInsnNode

/**
 * An abstraction that represents an instruction referencing a [ByteClass] that was scanned.
 * Also keeps a reference to the [ByteMethod] that this instruction is in with the [ClassReferenceNode.method]
 * property.
 */
class ClassReferenceNode(
    val method: ByteMethod,
    opcode: Int,
    descriptor: String?
) : TypeInsnNode(
    opcode,
    descriptor
), ByteNode {

    /**
     * Adds this Class Reference object to the [ByteClass] that it references.
     */
    fun addToClass() {
            val clazz = findClass(desc)
        if  (clazz != null) {

            clazz.typeReferences.add(this)
        }
    }
}