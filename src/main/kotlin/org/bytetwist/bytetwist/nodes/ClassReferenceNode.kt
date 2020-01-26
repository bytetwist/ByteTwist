package org.bytetwist.bytetwist.nodes

import org.bytetwist.bytetwist.References
import org.objectweb.asm.tree.TypeInsnNode

class ClassReferenceNode(
    method: CompiledMethod,
    opcode: Int,
    descriptor: String?
) : TypeInsnNode(
    opcode,
    descriptor
), CompiledNode {

    fun addToClass() {
        References.classNames.getOrElse(
            desc.replaceFirst("L", "", false).replace(";", ""), {
                null
            })?.typeReferences?.add(this)
    }
}