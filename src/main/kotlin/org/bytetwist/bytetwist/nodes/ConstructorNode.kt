package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode

/**
 * An abstraction of CompiledMethods that are constructors
 */
class ConstructorNode(
    parent: CompiledClass,
    access: Int,
    name: String,
    descriptor: String,
    signature: String?,
    exceptions: Array<out String>?
) : MethodNode(
    Opcodes.ASM7,
    access,
    name,
    descriptor,
    signature,
    exceptions
), CompiledNode {
}