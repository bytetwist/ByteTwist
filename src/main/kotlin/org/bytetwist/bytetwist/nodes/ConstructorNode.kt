package org.bytetwist.bytetwist.nodes


/**
 * A subtype of [ByteMethod] that represents a constructor method. This also includes static constructors. Will almost
 * always be named "<init>" or "<clinit>"
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