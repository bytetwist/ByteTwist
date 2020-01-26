package org.bytetwist.bytetwist.nodes


import com.google.common.graph.ValueGraphBuilder
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.SOURCE_MASK
import org.objectweb.asm.tree.*
import org.bytetwist.bytetwist.References
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet


/**
 * An abstraction of the ClassNode
 */
open class CompiledMethod(
    val parent: CompiledClass,
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
), CompiledNode {

    /**
     * A list of MethodReferences in which this method is invoked
     */
    val invocations = CopyOnWriteArraySet<MethodReferenceNode>()

    val exceptionThrowExpressions = CopyOnWriteArraySet<ThrowExpressionNode>()

    val blocks = LinkedHashSet<CompiledBlockNode>()

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
        getLabelNode(start)
        super.visitTryCatchBlock(start, end, handler, type)
    }

    override fun visitInsn(opcode: Int) {
        instructions.add(InsnNode(opcode))
        if (opcode == Opcodes.ATHROW) {
            addThrowExpressionNode(instructions.last)
        }
    }

    private fun addThrowExpressionNode(throwInstruction: AbstractInsnNode) {
        val throwInstructions = LinkedList<AbstractInsnNode>()
        if (throwInstruction.previous !is MethodInsnNode) {
            if (throwInstruction.previous.opcode == 25) {
                // We can't handle this until the entire method has been read. Need to implement later
                return
            }
        }
        val invokeSpecial = throwInstruction.previous
        if (invokeSpecial.opcode == Opcodes.INVOKESPECIAL) {
            throwInstructions.add(throwInstruction)
            throwInstructions.add(invokeSpecial)
            var dup = invokeSpecial.previous
            while (dup.opcode != Opcodes.NEW) {
                throwInstructions.add(dup)
                dup = dup.previous
            }
            dup as TypeInsnNode
            throwInstructions.add(dup)
            val throwExpression =
                ThrowExpressionNode(this, dup.desc, throwInstructions)
            exceptionThrowExpressions.add(throwExpression)
        }
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
        val annotationNode = MethodAnnotationNode(this, descriptor)
        if (visible) {
            visibleAnnotations.add(annotationNode)
        } else {
            invisibleAnnotations.add(annotationNode)
        }
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

    fun analyzeBlocks() {
        val cfg = ValueGraphBuilder.directed().build<CompiledBlockNode, AbstractInsnNode>()
        val mainBlock = CompiledBlockNode(this)
        var block = CompiledBlockNode(this)
        //val analyzer = Analyzer<SourceValue>(SourceInterpreter())
        //val frames = analyzer.analyze(this.parent.name, this)
        var i = 0
        while (i < (instructions.size())) {
            var insn = instructions[i]
            mainBlock.add(insn)
            if (insn !is JumpInsnNode) {
                block.add(insn)
            } else {
                block.add(insn)
                val target = insn.label as AbstractInsnNode
                val oldBlock = block
                block = CompiledBlockNode(this)
                cfg.putEdgeValue(oldBlock, block, insn)

                oldBlock.edges.add(insn to block)
                blocks.add(oldBlock)
                if (insn.opcode != Opcodes.GOTO) {
                    if (!blocks.contains(instructions.buildBlock(this, instructions.indexOf(target)))) {
                        //blocks.add(instructions.buildBlock(this, instructions.indexOf(target)))
                    }
                } else {
                    if (!blocks.contains(instructions.buildBlock(this, instructions.indexOf(target)))) {
                        i = instructions.indexOf(target)
                        continue
                    }
                }
            }
            i++
            if (insn == instructions.last) {
                mainBlock.add(insn)
                blocks.add(mainBlock)

            }
        }
        blocks.forEach {
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
     * Moves this method to the specified CompiledClass and updates all references to this method
     * @param destination - The CompiledClass object
     */
    fun move(destination: CompiledClass) {
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
}