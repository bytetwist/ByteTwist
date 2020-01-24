package org.bytetwist.bytetwist.nodes

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
) : CompiledMethod(parent, access, name, descriptor, signature, exceptions) {
}