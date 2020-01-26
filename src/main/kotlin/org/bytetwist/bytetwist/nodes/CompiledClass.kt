package org.bytetwist.bytetwist.nodes

import com.google.common.annotations.Beta
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.processors.log
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import java.lang.reflect.Modifier
import java.util.concurrent.CopyOnWriteArraySet

/**
 * An Abstraction of the ClassNode
 */
open class CompiledClass : ClassNode(Opcodes.ASM7), CompiledNode {

    val subClasses = CopyOnWriteArraySet<CompiledClass>()

    val typeReferences = CopyOnWriteArraySet<ClassReferenceNode>()

    val constructors = methods.filterIsInstance(ConstructorNode::class.java)

    /**
     * Visits the class and adds it to the list of CompiledClasses in the References object
     */
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        References.classNames[name] = this
        super.visit(version, access, name, signature, superName, interfaces)
    }

    /**
     * Visits a class field, adds it to the References, and adds it to this classes list of fields
     */
    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        val field = CompiledField(
            this,
            access,
            name,
            descriptor,
            signature,
            value
        )
        References.fieldNames[this.name + "." + name] = field
        fields.add(field)
        return field
    }

    /**
     *
     */
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        val classAnnotation = ClassAnnotationNode(this, descriptor)
        if (visible) {
            visibleAnnotations.add(classAnnotation)
        } else {
            invisibleAnnotations.add(classAnnotation)
        }
        return classAnnotation
    }

    /**
     * Visits a class method, determines if it is a method or a constructor, adds it to the References, and adds
     * it to this classes list of methods/constructors
     */
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (name.contains("<init>") || name.contains("<clinit>")) {
            val constructor = ConstructorNode(
                this,
                access,
                name,
                descriptor,
                signature,
                exceptions
            )
            methods.add(constructor)
            return constructor
        }
        val method = CompiledMethod(
            this,
            access,
            name,
            descriptor,
            signature,
            exceptions
        )
        References.methodNames[this.name + "." + name + "." + descriptor] = method
        methods.add(method)
        return method
    }

    @Beta
    fun rename(newName: String) {
        val oldName = this.name
        subClasses.forEach {
            it.superName = newName
        }
        this.name = newName
        References.classNames.remove(oldName)
        References.classNames[this.name] = this
        this.fields.forEach { fieldNode ->
            if (fieldNode is CompiledField) {
                References.fieldNames.remove("$oldName.${fieldNode.name}")
                References.fieldNames["${name}.${fieldNode.name}"] = fieldNode
                fieldNode.references.forEach {
                    it.owner = newName
                    it.addToField()
                }
            }
        }
        this.methods.forEach { methodNode ->
            if (methodNode is CompiledMethod) {
                References.methodNames.remove("$oldName.${methodNode.name}.${methodNode.desc}")
                References.methodNames["$name.${methodNode.name}.${methodNode.desc}"] = methodNode
                methodNode.invocations.forEach {
                    it.owner = newName
                    it.addToMethod()
                    if (it.desc.contains(oldName)) {
                        it.desc = it.desc.replace("L$oldName;", "L$name;")
                    }
                }
            }
        }
        typeReferences.forEach {
            it.desc = it.desc.replace("L$oldName;", "L$name;")
        }


        References.methodNames.values.forEach {
            if (it.desc.contains(oldName)) {
                it.desc = it.desc.replace("L$oldName;", "L$name;")
            }
            if (it.signature != null) {
                it.signature = it.signature.replace("L$oldName", "L$newName")
            }
        }
        References.fieldNames.values.forEach {
            if (it.desc.contains(oldName)) {
                it.desc = it.desc.replace("L$oldName;", "L$name;")
            }
            if (it.signature != null) {
                it.signature = it.signature.replace("L$oldName", "L$newName")
            }
        }

//            }

    }

    /**
     * Returns the super class of this class, if the super class is one of the classes scanned. Returns null otherwise
     * If you are trying to return just the name of the super class, use the ASM implementation of superName
     * @return The CompiledClass object of the super class if the super class was one of the scanned classes, otherwise
     * null
     */
    fun superClass(): CompiledClass? = References.classNames.getOrDefault(superName, null)


    /**
     * Scans this class with a ClassWriter and returns the array of bytes
     */
    fun toBytes(): ByteArray {
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        this.accept(cw)
        return cw.toByteArray()
    }

    /**
     * Returns if this class is an interface or not. Doesn't seem to be working
     */
    fun isInterface() = Modifier.isInterface(access)

    fun isEnum() = access.and(Opcodes.ACC_ENUM) == Opcodes.ACC_ENUM

    /**
     * Returns if this class is abstract or not
     */
    fun isAbstract() = Modifier.isAbstract(access)

    fun buildHierarchy() {
        val classes = References.classNames.values.filter { compiledClass ->
            compiledClass.superName == this.name
        }
        this.subClasses.addAll(classes)
    }
}