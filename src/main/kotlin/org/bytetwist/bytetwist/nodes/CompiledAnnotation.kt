package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode

open class CompiledAnnotation(
    val annotates: CompiledNode,
    var descriptor: String?
) : AnnotationNode(Opcodes.ASM7, descriptor), CompiledNode {

    fun field(name: String, value: Any?) {
        super.visit(name, value)
    }


    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)
    }
}

class ClassAnnotationNode(
    annotates: CompiledClass,
    descriptor: String?
) : CompiledAnnotation(
    annotates,
    descriptor
), CompiledNode

class FieldAnnotationNode(
    annotates: CompiledField,
    descriptor: String?
) : CompiledAnnotation(
    annotates,
    descriptor
), CompiledNode

class MethodAnnotationNode(
    annotates: CompiledNode,
    descriptor: String?
) : CompiledAnnotation(
    annotates,
    descriptor
), CompiledNode