package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.Type


/**
 * A subtype of [ByteMethod] that represents a constructor method. This also includes static constructors. Will almost
 * always be named "<init>" or "<clinit>"
 */
class ByteConstructor(
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
), ByteNode {


    override fun annotate(
        name: String,
        vararg fieldsToValues: Pair<String, Any>
    ) {
        with(this.visitAnnotation(Type.getObjectType(name).descriptor, true)) {
            fieldsToValues.asIterable().forEach {
                this.visit(it.first, it.second)
            }
            this.visitEnd()
        }
    }
}