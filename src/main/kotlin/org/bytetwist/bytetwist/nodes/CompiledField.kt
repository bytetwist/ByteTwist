package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode
import org.bytetwist.bytetwist.References
import org.objectweb.asm.tree.AnnotationNode
import java.lang.reflect.Modifier
import java.util.concurrent.CopyOnWriteArraySet

/**
 * An abstraction of the FieldNode that includes a list of @see FieldReferenceNode references to each access of this
 * field, as well as a property of the owning CompiledClass: parent
 */
class CompiledField(
    val parent: CompiledClass,
    access: Int,
    name: String,
    descriptor: String,
    signature: String?,
    value: Any?
) : FieldNode(Opcodes.ASM7, access, name, descriptor, signature, value),
    CompiledNode {

    /**
     * A Set of all FieldReferenceNode's that reference this particular field
     */
    val references = CopyOnWriteArraySet<FieldReferenceNode>()

    init {
        visibleAnnotations = mutableListOf<AnnotationNode>()
        invisibleAnnotations = mutableListOf<AnnotationNode>()
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val annotationNode = FieldAnnotationNode(this, descriptor)
        if (visible) {
            visibleAnnotations.add(annotationNode)
        } else {
            invisibleAnnotations.add(annotationNode)
        }
        return annotationNode
    }

    /**
     * Changes the name of this field in it's declaration and in any instructions that reference it @see FieldReferenceNode
     * @param newName - The new name the field will be changed to
     */
    fun rename(newName: String) {
        val oldName = name
        this.name = newName
        references.forEach {
            it.name = newName
        }
        References.fieldNames.remove(oldName)
        References.fieldNames["${parent.name}.$name"] = this
    }

//    fun annotate(init: CompiledAnnotation.(CompiledField, String) -> Unit, descriptor: String): CompiledAnnotation {
//
//
//        val annotation = CompiledAnnotation(this, descriptor)
//
//    }

    /**
     * Removes this CompiledField/FieldNode from its parent class and removes all references to the field
     */
    fun delete() {
        parent.fields.remove(this)
        references.forEach {
            it.method.instructions.remove(it)
        }
        References.fieldNames.remove(name)
    }

    /**
     * Moves this Field to the specified CompiledClass and updates all the references to the field
     * @param destination - The CompiledClass object to move the field to
     */
    fun move(destination: CompiledClass) {
        References.fieldNames.remove("${parent.name}.$name")
        destination.visitField(access, name, desc, signature, value)
        this.references.forEach {
            it.owner = destination.name
            it.addToField()
        }
        parent.fields.remove(this)
    }

    /**
     * Returns true if the field is abstract, false otherwise
     */
    fun isAbstract() = Modifier.isAbstract(access)

    /**
     * Sets the field as Abstract/non-abstract
     */
    fun setAbstract() {
        access = access.or(Modifier.ABSTRACT)
    }

    /**
     * Returns true if the field has a public access modifier, false otherwise
     */
    fun isPublic() = Modifier.isPublic(access)

    /**
     * Adds a public access modifier to the field
     */
    fun setPublic() {
        access = access.or(Modifier.PUBLIC)
    }

    /**
     * Returns true if the field has a private access modifier, false otherwise.
     */
    fun isPrivate() = Modifier.isPrivate(access)

    /**
     * Adds a private access modifier to the field
     */
    fun setPrivate() {
        access = access.or(Modifier.PRIVATE)
    }

    /**
     * Returns true if the field has a static access modifier, false otherwise.
     */
    fun isStatic() = Modifier.isStatic(access)

    /**
     * Adds a static access modifier to the field.
     */
    fun setStatic() {
        access = access.or(Modifier.STATIC)
    }

    fun annotate(
        annotationName: String,
        vararg fields: Pair<String, *>
    ) {
        val ann = visitAnnotation(annotationName, true) as FieldAnnotationNode
        ann.visitEnd()
        if (fields.any()) {
            for (field in fields) {
                ann.field(field.first, field.second)
            }
        }
        this.visibleAnnotations.add(ann)
    }

}




