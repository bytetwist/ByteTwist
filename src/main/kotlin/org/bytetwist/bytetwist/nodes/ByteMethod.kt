package org.bytetwist.bytetwist.nodes


import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.processors.log
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.SOURCE_MASK
import org.objectweb.asm.Type
import org.objectweb.asm.commons.TryCatchBlockSorter
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.*
import java.lang.reflect.Modifier
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.collections.HashSet


/**
 * An abstraction of the ClassNode
 */
open class ByteMethod(
        val parent: ByteClass,
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
) : MethodNode(
    Opcodes.ASM7,
    access,
    name,
    descriptor,
    signature,
    exceptions
), ByteNode {

    /**
     * A list of MethodReferences in which this method is invoked
     */
    val invocations = CopyOnWriteArraySet<MethodReferenceNode>()

    /**
     * A HashSet of all of the [ByteBlockNode] that make up this method (excluding try/catch blocks)
     */
    val blocks = HashSet<Block>()

    /**
     * A list of all references to fields that this method makes. (Includes Field Reads/Writes)
     */
    fun fieldReferences() = instructions.filterIsInstance(FieldReferenceNode::class.java)

    /**
     * A List of all Field Reads this method performs
     */
    fun fieldReads() = instructions.filterIsInstance(FieldRead::class.java)

    /**
     * A List of all field writes this method performs
     */
    fun fieldWrites() = instructions.filterIsInstance(FieldWrite::class.java)

    /**
     * A list of all invocations of other methods that this method makes
     */
    fun methodCalls() = instructions.filterIsInstance(MethodReferenceNode::class.java)

    fun typeReferences() = instructions.filterIsInstance(ClassReferenceNode::class.java)


    init {
        visibleAnnotations = mutableListOf<AnnotationNode>()
        invisibleAnnotations = mutableListOf<AnnotationNode>()
    }


    override fun visitMethodInsn(
        opcodeAndSource: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        val methodCall = MethodReferenceNode(
            this, opcodeAndSource and SOURCE_MASK.inv(),
            owner, name, descriptor, isInterface
        )
        instructions.add(methodCall)
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
        instructions.indexOf(getLabelNode(start))
        if (type != null) {
            val tryCatch = ByteTryCatch(this, getLabelNode(start),
                getLabelNode(end), getLabelNode(handler), type)
            this.tryCatchBlocks.add(tryCatch)
            return
        }
        this.tryCatchBlocks.add(ByteTryCatch(this, getLabelNode(start),
            getLabelNode(end), getLabelNode(handler), ""))
    }

    override fun visitInsn(opcode: Int) {
        instructions.add(InsnNode(opcode))

    }



    override fun visitLocalVariable(
        name: String?,
        descriptor: String?,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int
    ) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index)
    }


    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val annotationNode = ByteAnnotation(this, descriptor)
        visibleAnnotations.add(annotationNode)
        return annotationNode
    }

    override fun getLabelNode(label: Label?): LabelNode {
        return super.getLabelNode(label)
    }

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String?) {
        when (opcode) {
            in FieldOpcodes.READ_CODES -> instructions.add(
                FieldRead(this, opcode, owner, name, descriptor)
            )
            in FieldOpcodes.WRITE_CODES -> instructions.add(
                FieldWrite(this, opcode, owner, name, descriptor)
            )
        }
        (instructions.last as FieldReferenceNode).addToField()
    }

    fun buildBlocks() {
        // Sort try catch blocks
        //this.accept(TryCatchBlockSorter(this, access, name, desc, signature, exceptions.toTypedArray()))
        var block = ByteBlockNode(this)
        for (insnNode in instructions) {
            when (insnNode) {
                instructions.first -> {
                    block = ByteBlockNode(this)
                    block.add(insnNode)
                }
                is JumpInsnNode -> {
                    block.add(insnNode)
                    blocks.add(block)
                    block = ByteBlockNode(this)
                }
                else -> {
                    block.add(insnNode)
                    if (insnNode == instructions.last) {
                        blocks.add(block)
                    }
                }
            }
        }
        try {
            analyzer.analyze(this.parent.name, this)
           // annotate("Complexity", "Blocks" to blocks.size, "Edges" to blocks.flatMap { it.edges }.count())

        } catch (e: AnalyzerException) {
            log.error { e.message + " ${e.node} + ${e.node.opcode}" }
        }


    }

    override fun visitAnnotationDefault(): AnnotationVisitor {
        return super.visitAnnotationDefault()
    }


    override fun visitLineNumber(line: Int, start: Label?) {

    }

    private val analyzer = object : Analyzer<BasicValue>(BasicVerifier()) {

        override fun newControlFlowEdge(insnIndex: Int, successorIndex: Int) {
            findBlockByLast(instructions[insnIndex])?.let {
                val b = findBlock(instructions[successorIndex])
                if (b?.edges?.add(it to EdgeDirection.OUT) == true) {
                    it.edges.add(b to EdgeDirection.IN)
                }
            }
        }

        override fun newControlFlowExceptionEdge(insnIndex: Int, tryCatchBlock: TryCatchBlockNode?): Boolean {
            return super.newControlFlowExceptionEdge(insnIndex, tryCatchBlock)
        }

        override fun newControlFlowExceptionEdge(insnIndex: Int, successorIndex: Int): Boolean {
            return super.newControlFlowExceptionEdge(insnIndex, successorIndex)
        }
    }


    override fun visitTypeInsn(opcode: Int, type: String?) {
        val classReference = ClassReferenceNode(this, opcode, type)
        instructions.add(classReference)
    }

    override fun visitParameter(name: String?, access: Int) {

        super.visitParameter(name, access)
    }


    /**
     * Renames this method and all invocations of this method
     */
    fun rename(newName: String) {
        val oldName = name
        this.name = newName
        References.methodNames.remove("${parent.name}.${oldName}.${desc}")
        References.methodNames["${parent.name}.${newName}.${desc}"] = this
        this.invocations.forEach {
            it.name = newName
            it.addToMethod()
        }
    }

    /**
     * Removes this method as well as all invocations of this method in all scanned files
     */
    fun delete() {
        invocations.forEach {
            it.getMethod()?.instructions?.remove(it)
        }
        References.methodNames.remove("${parent.name}.${this.name}.${this.desc}")
        parent.methods.remove(this)
    }

    /**
     * Moves this method to the specified ByteClass and updates all references to this method
     * @param destination - The ByteClass object
     */
    fun move(destination: ByteClass) {
        References.methodNames.remove("${parent.name}.$name.$desc")
        destination.visitMethod(access, name, desc, signature, exceptions.toTypedArray()).visitEnd()
        this.invocations.forEach {
            it.owner = destination.name
            it.addToMethod()
        }
        References.findMethod(this.name)?.instructions?.add(this.instructions)
        parent.methods.remove(this)
    }

    fun isAbstract() = Modifier.isAbstract(access)

    fun setAbstract() {
        access = access.or(Modifier.ABSTRACT)
    }

    fun isPublic() = Modifier.isPublic(access)

    fun setPublic() {
        access = access.or(Modifier.PUBLIC)
    }

    fun isPrivate() = Modifier.isPrivate(access)

    fun setPrivate() {
        access = access.or(Modifier.PRIVATE)
    }

    fun isStatic() = Modifier.isStatic(access)

    fun setStatic() {
        access = access.or(Modifier.STATIC)
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