package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode

open class ByteAnnotation(
    val annotates: ByteNode,
    var descriptor: String?
) : AnnotationNode(
    Opcodes.ASM7,
    descriptor
), ByteNode {

    fun field(name: String, value: Any?) {
        super.visit(name, value)
    }


    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)
    }
}

class ClassAnnotationNode(
    annotates: ByteClass,
    descriptor: String?
) : ByteAnnotation(
    annotates,
    descriptor
), ByteNode

class FieldAnnotationNode(
    annotates: ByteField,
    descriptor: String?
) : ByteAnnotation(
    annotates,
    descriptor
), ByteNode

class MethodAnnotationNode(
    annotates: ByteNode,
    descriptor: String?
) : ByteAnnotation(
    annotates,
    descriptor
), ByteNode