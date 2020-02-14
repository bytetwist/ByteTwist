package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode
import org.bytetwist.bytetwist.References
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import java.lang.reflect.Modifier
import java.util.concurrent.CopyOnWriteArraySet

/**
 * An abstraction of the FieldNode that includes a list of @see FieldReferenceNode references to each access of this
 * field, as well as a property of the owning ByteClass: parent
 */
class ByteField(
        val parent: ByteClass,
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
) : FieldNode(Opcodes.ASM7, access, name, descriptor, signature, value),
    ByteNode {

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

//    fun annotate(init: ByteAnnotation.(ByteField, String) -> Unit, descriptor: String): ByteAnnotation {
//
//
//        val annotation = ByteAnnotation(this, descriptor)
//
//    }

    /**
     * Removes this ByteField/FieldNode from its parent class and removes all references to the field
     */
    fun delete() {
        parent.fields.remove(this)
        references.forEach {
            it.method.instructions.remove(it)
        }
        References.fieldNames.remove("${parent.name}.${this.name}")
        parent.fields.remove(this)
    }

    /**
     * Moves this Field to the specified ByteClass and updates all the references to the field
     * @param destination - The ByteClass object to move the field to
     */
    fun move(destination: ByteClass) {
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
        access = access.rem(Modifier.STATIC)
    }

    fun annotate(name: String,
                 vararg fieldsToValues: Pair<String, *>) {
        with(this.visitAnnotation(Type.getObjectType(name).descriptor, true)) {
            fieldsToValues.asIterable().forEach {
                this.visit(it.first, it.second)
            }
            this.visitEnd()
        }
    }
}




