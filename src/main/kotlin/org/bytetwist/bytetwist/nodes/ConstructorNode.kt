package org.bytetwist.bytetwist.nodes


/**
 * An abstraction of [ByteMethod] that is a constructors
 */
class ConstructorNode(
        parent: ByteClass,
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
) : ByteMethod(
    parent,
    access,
    name,
    descriptor,
    signature,
    exceptions
), ByteNode