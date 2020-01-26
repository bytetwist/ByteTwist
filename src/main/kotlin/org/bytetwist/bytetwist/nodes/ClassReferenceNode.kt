package org.bytetwist.bytetwist.nodes

import org.bytetwist.bytetwist.References
import org.objectweb.asm.tree.TypeInsnNode

/**
 * An abstraction that represents an instruction referencing a [CompiledClass] that was scanned.
 * Also keeps a reference to the [CompiledMethod] that this instruction is in with the [ClassReferenceNode.method]
 * property.
 */
class ClassReferenceNode(
    val method: CompiledMethod,
    opcode: Int,
    descriptor: String?
) : TypeInsnNode(
    opcode,
    descriptor
), CompiledNode {

    /**
     * Adds this Class Reference object to the [CompiledClass] that it references.
     */
    fun addToClass() {
        References.classNames[
                desc.replaceFirst("L", "", false)
                    .replace(";", "")]?.typeReferences?.add(this)
    }
}