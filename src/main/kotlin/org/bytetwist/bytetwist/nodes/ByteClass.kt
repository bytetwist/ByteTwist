package org.bytetwist.bytetwist.nodes

import com.google.common.annotations.Beta
import kotlinx.coroutines.*
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.Settings
import org.bytetwist.bytetwist.processors.log
import org.bytetwist.bytetwist.scanners.DoublePassScanner
import org.objectweb.asm.*
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.collections.ArrayList
import kotlin.coroutines.suspendCoroutine

/**
 * An Abstraction of the ClassNode. All of the objects in the methods field can be cast to [ByteMethod] and all
 * the objects in the fields field can be cast to [ByteField].
 *
 * A ByteClass is part of an inheritance hierarchy. It's subclasses can be accessed through
 * the [subClasses] property and it's parent class (if it has one) can be accessed through [superClass].
 * If the class has a parent class that was not scanned by ByteTwist then the only reference to the parent class
 * is through the field [superName].
 *
 * A ByteClass also has access to all instructions that reference the class in the form of a [ClassReferenceNode],
 * which are stored in the [typeReferences] property.
 *
 * The ByteClass has many methods that allow modification of the class without having to worry about references.
 *
 * TODO: Interfaces, Enums, DSL/methods for easily generating new CompiledClasses
 */
class ByteClass(
    name: String = "",
    signature: String? = null,
    access: Int = Modifier.PUBLIC,
    superName: String = "Object",
    interfaces: Array<String> = emptyArray()
) : ClassNode(Opcodes.ASM7), ByteNode {

    /**
     * A list of scanned [ByteClass] that extend this class.
     */
    val subClasses = CopyOnWriteArraySet<ByteClass>()

    val implementedBy = CopyOnWriteArraySet<ByteClass>()

    val typeReferences = CopyOnWriteArraySet<ClassReferenceNode>()

    val constructors: List<ByteConstructor>
        get() = super.methods.filterIsInstance(ByteConstructor::class.java)

    init {
        References.classNames[name] = this
        super.visit(super.version, access, name, signature, superName, interfaces)
    }

    init {
        visibleAnnotations = mutableListOf<AnnotationNode>()
        invisibleAnnotations = mutableListOf<AnnotationNode>()
    }

    /**
     * Visits the class and adds it to the list of CompiledClasses in the [References] object
     */
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<String>
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
        val field = ByteField(
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
     * Visits an annotation and adds it to  [visibleAnnotations] or [invisibleAnnotations] as a [ClassAnnotationNode]
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
     * Visits a class method or constructor and builds a [ByteMethod] or [ByteConstructor] object.
     * Note: constructors are not represented as subtypes of CompiledMethods
     * it to this classes list of methods/constructors
     */
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        if (name.contains("<init>") || name.contains("<clinit>")) {
            return constructorNode(access, name, descriptor, signature, exceptions)
        }
        return compiledMethod(access, name, descriptor, signature, exceptions)
    }

    private fun compiledMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): ByteMethod {
        val method = ByteMethod(
            this,
            access,
            name,
            descriptor,
            signature,
            exceptions
        )
        References.methodNames[this.name + "." + name + "." + descriptor] = method
        this.methods.add(method)
        return method
    }

    private fun constructorNode(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): ByteConstructor {
        val constructor = ByteConstructor(
            this,
            access,
            name,
            descriptor,
            signature,
            exceptions
        )
        this.methods.add(constructor)
        return constructor
    }

    @Beta
    fun rename(newName: String) {
        val oldName = this.name

        subClasses.forEach {
            it.superName = newName
        }

        if (Settings.annotateClassChanges) {
            annotate("Renamed", "Old Name" to oldName)
        }

        this.name = newName
        References.classNames.remove(oldName)
        References.classNames[this.name] = this

        fields.forEach { fieldNode ->
            if (fieldNode is ByteField) {
                References.fieldNames.remove("$oldName.${fieldNode.name}")
                References.fieldNames["${name}.${fieldNode.name}"] = fieldNode
                Collections.synchronizedList(fieldNode.references).forEach {
                    it.owner = newName
                    //it.addToField()
                }
            }
        }

        methods.filterIsInstance(ByteMethod::class.java).forEach { methodNode ->
            References.methodNames.remove("$oldName.${methodNode.name}.${methodNode.desc}")
            References.methodNames["$name.${methodNode.name}.${methodNode.desc}"] = methodNode
            methodNode.invocations.forEach {
                it.owner = newName
                it.addToMethod()
                if (it.desc.contains(oldName)) {
                    it.desc = it.desc.replace("L$oldName;", "L$name;")
                }
            }
            if (methodNode.signature != null && methodNode.signature.contains(Type.getObjectType(oldName).descriptor)) {
                methodNode.signature = methodNode.signature.replace(
                    Type.getObjectType(oldName).descriptor,
                    Type.getObjectType(newName).descriptor
                )
            }
        }

        typeReferences.forEach {
            it.desc = it.desc.replace("$oldName", "$name")
        }

        References.methodNames.values.forEach {
            if (it.desc.contains(oldName)) {
                it.desc = it.desc.replace("L$oldName;", "L$name;")
                it.invocations.forEach { m ->
                    m.desc = m.desc.replace("L$oldName;", "L$name;")
                }
            }
            if (it.signature != null && it.signature.contains("L$oldName;")) {
                it.signature = it.signature.replace("L$oldName", "L$newName")
            }
        }

        References.fieldNames.values.forEach {
            if (it.desc.contains(oldName)) {
                it.desc = it.desc.replace("L$oldName;", "L$name;")
                it.references.forEach { ref ->
                    ref.desc = ref.desc.replace("L$oldName;", "L$name;")
                }
            }
//            log.info { it.desc }
//            log.info { it.signature }
            if (it.signature != null && it.signature.contains("L$oldName;")) {
                it.signature = it.signature.replace("L$oldName", "L$newName")
            }
        }


//            }
    }


    /**
     * Returns the super class of this class, if the super class is one of the classes scanned. Returns null otherwise
     * If you are trying to return just the name of the super class, use the ASM implementation of superName
     * @return The ByteClass object of the super class if the super class was one of the scanned classes, otherwise
     * null
     */
    fun superClass(): ByteClass? = References.classNames.getOrDefault(superName, null)


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

    fun isEnum() = access.and(Opcodes.ACC_ENUM) != 0

    /**
     * Returns if this class is abstract or not
     */
    fun isAbstract(): Boolean {
        return Modifier.isAbstract(access)
    }

    fun buildHierarchy() {
        val classes = References.classNames.values.filter { compiledClass ->
            compiledClass.superName == this@ByteClass.name
        }
        this@ByteClass.subClasses.addAll(classes)
    }

    fun annotate(
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

class ByteClassBuilder() {
    lateinit var name: String
    private var signature: String? = null
    private var access: Int = Modifier.PUBLIC
    private var superName: String = "Object"
    private var interfaces: Array<String> = emptyArray()
    fun build() = ByteClass(name, signature, access, superName, interfaces)
}

@InternalCoroutinesApi
inline fun newClass(init: ByteClassBuilder.() -> Unit): ByteClass {
    val byteClass = ByteClassBuilder().apply(init).build()
    References.classNames.putIfAbsent(byteClass.name, byteClass)
    return byteClass
}

