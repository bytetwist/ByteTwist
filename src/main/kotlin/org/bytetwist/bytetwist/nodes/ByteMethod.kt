package org.bytetwist.bytetwist.nodes


import com.mxgraph.layout.*
import com.mxgraph.model.mxICell
import com.mxgraph.util.mxCellRenderer
import kotlinx.coroutines.*
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.Settings
import org.bytetwist.bytetwist.processors.log
import org.jgrapht.alg.color.BrownBacktrackColoring
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedAcyclicGraph
import org.jgrapht.nio.dot.DOTExporter
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.SOURCE_MASK
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.io.File
import java.io.StringWriter
import java.lang.reflect.Modifier
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Function
import javax.imageio.ImageIO


/**
 * An abstraction of the ClassNode
 * @param parent: The [ByteClass] that this method is a member of
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
    val blocks = CopyOnWriteArraySet<Block>()

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

    /**
     * A list of [ClassReferenceNode] that this method's instructions contains
     */
    fun typeReferences() = instructions.filterIsInstance(ClassReferenceNode::class.java)

    val controlFlow: DirectedAcyclicGraph<ByteBlockNode, DefaultEdge> = DirectedAcyclicGraph(DefaultEdge::class.java)


    init {
        visibleAnnotations = mutableListOf<AnnotationNode>()
        invisibleAnnotations = mutableListOf<AnnotationNode>()
    }

    /**
     * Visits a [ByteVariableReference] and adds it to this methods instructions
     */
    override fun visitVarInsn(opcode: Int, `var`: Int) {
        val reference = ByteVariableReference(opcode, `var`)
        instructions.add(reference)
    }

    /**
     * Visits a method instruction and adds it to this ByteMethods list of instructions as a [MethodReferenceNode].
     *
     */
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

    /**
     * Visits a try catch block and creates a [ByteTryCatch] then adds it to this methods list of try catch blocks [tryCatchBlocks]
     */
    override fun visitTryCatchBlock(
        start: Label?,
        end: Label?,
        handler: Label?,
        type: String?
    ) {
        instructions.indexOf(getLabelNode(start))
        if (type != null) {
            val tryCatch = ByteTryCatch(
                this, getLabelNode(start),
                getLabelNode(end), getLabelNode(handler), type
            )
            this.tryCatchBlocks.add(tryCatch)
            return
        }
        this.tryCatchBlocks.add(
            ByteTryCatch(
                this, getLabelNode(start),
                getLabelNode(end), getLabelNode(handler), "Exception"
            )
        )
    }

    /**
     * TODO: this
     */
    override fun visitLocalVariable(
        name: String,
        descriptor: String,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int
    ) {
        val byteVar = ByteVariable(
            this,
            name,
            descriptor,
            signature,
            getLabelNode(start),
            getLabelNode(end),
            index
        )
        localVariables.add(byteVar)
    }

    /**
     * Adds all other InsnNodes to this methods instruction list
     */
    override fun visitInsn(opcode: Int) {
        instructions.add(InsnNode(opcode))
    }

    /**
     * Visits an annotation and creates a ByteAnnotation if the annotation is visible. Then adds it to this methods list
     * of visible annotations [visibleAnnotations].
     */
    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val annotationNode = MethodAnnotationNode(this, descriptor)
        visibleAnnotations.add(annotationNode)
        return annotationNode
    }

    /**
     * Visits a field instruction to create a [FieldReferenceNode] and then add it to the list of instructions, as well as
     * the maintained lists of references in [References]
     */
    override fun visitFieldInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String?
    ) {
        when (opcode) {
            in FieldOpcodes.READ_CODES -> {
                val fieldRead = FieldRead(this, opcode, owner, name, descriptor)
                instructions.add(
                    fieldRead
                )
            }
            in FieldOpcodes.WRITE_CODES -> {
                val fieldWrite = FieldWrite(this, opcode, owner, name, descriptor)
                instructions.add(
                    fieldWrite
                )
            }
        }
        //(instructions.last as FieldReferenceNode).addToField()
    }

    /**
     * Builds all of the [ByteBlockNode] and [ByteTryCatch] blocks for this method. Uses coroutines to execute some parts
     * of the method asynchronously
     */
    fun buildBlocks() {
        runBlocking {
            //   coroutineScope {
            // Sort try catch blocks
            //this.accept(TryCatchBlockSorter(this, access, name, desc, signature, exceptions.toTypedArray()))
            var block: ByteBlockNode? = null
            loop@ for (insnNode in instructions) {
                when (insnNode) {
                    instructions.first -> block = buildBlock(insnNode)
                    is JumpInsnNode -> {
                        if (block == null) {
                            block = find(insnNode) ?: buildBlock(insnNode)
                        }
                        val block2 = buildBlock(insnNode.label)
                        controlFlow.addVertex(block)
                        controlFlow.addVertex(block2)
                        if (insnNode.next != null && insnNode.opcode != Opcodes.GOTO) {
                            if (insnNode.next != insnNode.label.next) {
                                val nextBlock = find(insnNode.next) ?: buildBlock(insnNode.next)
                                controlFlow.addVertex(nextBlock)
                                block = nextBlock
                            }
                        }

                    }
                    else -> continue@loop
                }
            }
            //log.info { "$name: ${blocks.size}" }


//        if (!this@ByteMethod.tryCatchBlocks.isNullOrEmpty()) {
//            this@ByteMethod.tryCatchBlocks.onEach { tryCatchBlock ->
//                //     async {
//                if (tryCatchBlock != null) {
//                    val tryCatchBlocks = (tryCatchBlock as ByteTryCatch).buildMethodBlocks()
//                    controlFlow.addVertex(tryCatchBlocks.first)
//                    controlFlow.addVertex(tryCatchBlocks.second)
//                }
//                //   }
//            }
//        }

            try {
                analyzer.analyze(this@ByteMethod.parent.name, this@ByteMethod)

                if (Settings.annotateMethodComplexity) {
                    //   launch {
                    annotate("Complexity", "Blocks" to blocks.size, "Edges" to controlFlow.edgeSet().size)
                    // }
                }

                if (Settings.annotateTryCatchCount) {
                    //   launch {
                    annotate("TryCatchs", "number" to tryCatchBlocks.size)
                    // }
                }

            } catch (e: AnalyzerException) {
            }

            if (Settings.annotateLocalVariables) {
                //      launch {
                if (localVariables != null && localVariables.isNotEmpty()) {
                    localVariables.onEach {
                        //     async {
                        annotate(
                            "Local Variable ${it.name}",
                            "References" to (it as ByteVariable).references.size
                        )
                    }
                    //    }.awaitAll()
                    //  }
                }
            }
            //References.blocks.addAll(this@ByteMethod.blocks)
        }
    }

    private fun buildBlock(startingInsn: AbstractInsnNode): ByteBlockNode {
        val newSet = ByteBlockNode(this@ByteMethod)
        newSet.add(startingInsn)
        var nxt = startingInsn.next
        while (nxt != null) {
            if (nxt is JumpInsnNode) {
                newSet.add(nxt)
                break
            }
            newSet.add(nxt)
            nxt = nxt.next
        }
        blocks.add(newSet)
        return newSet
    }


    // }

    /**
     * Do nothing, we don't want to keep line numbers
     */
    override fun visitLineNumber(line: Int, start: Label?) {
        super.visitLineNumber(line, start)
    }

    /**
     * An Analyzer that builds a cfg for this [ByteMethod] and adds the edges to all of this methods blocks.
     *
     */
    private val analyzer = object : Analyzer<BasicValue>(BasicInterpreter()) {

        override fun newControlFlowExceptionEdge(insnIndex: Int, successorIndex: Int): Boolean {
            val first = find(instructions[insnIndex])
            val second = find(instructions[successorIndex]) ?: buildBlock(instructions[successorIndex])
            controlFlow.addVertex(second)
            if (first != null && first != second) {
                controlFlow.addEdge(first, second)
            }
            return true
        }


        /**
         * Adds a control flow edge from one block to another
         */
        override fun newControlFlowEdge(insnIndex: Int, successorIndex: Int) {
            val first = find(instructions[insnIndex])
            val second = find(instructions[successorIndex]) ?: buildBlock(instructions[successorIndex])
            controlFlow.addVertex(second)
            if (first != null && first != second) {
                controlFlow.addEdge(first, second)
            }


            //   controlFlow.addEdge(single, next)
            // log.info { single.toString() + " -> " + ByteBlockNode(this@ByteMethod, succ) }
            // val successor = instructions[successorIndex]
            // val first = blocks.find { a -> a.last() == last }
//            val second = blocks.single { b -> b.first() == successor }
//            log.info { last }
//            controlFlow.addVertex(first)
//            controlFlow.addVertex(second)
//            controlFlow.addEdge(first, second)
        }

//        override fun newControlFlowExceptionEdge(insnIndex: Int, successorIndex: Int): Boolean {
//            val first = findBlockByLast(instructions[insnIndex])
//            val second = findBlock(instructions[successorIndex])
//            if (first != null) {
//                if (second != null) {
//                    controlFlow.addEdge(first, second)
//
//                    cfg.addEdge(first, second, DefaultEdge())
//                }
//            }
//            return true
//        }
//
//        /**
//         * Adds an edge from a try block to it's catch handler
//         */
//        override fun newControlFlowExceptionEdge(insnIndex: Int, successorIndex: Int): Boolean {
//            jgraph.addEdge(findBlock(instructions[insnIndex]), findBlock(instructions[successorIndex]))
////            findBlockByLast(instructions[insnIndex])?.let {
////                val b = findBlock(instructions[successorIndex])
////                if (it.edges.add((b to EdgeDirection.OUT) as Pair<ByteBlockNode, EdgeDirection>) ) {
////                    //it.edges.add(b to EdgeDirection.IN)
////                    return true
////                }
////            }
//            return true
//        }
    }

    /**
     * Visits a type instruction to create a [ClassReferenceNode] and adds it to the [instructions]
     */
    override fun visitTypeInsn(opcode: Int, type: String?) {
        val classReference = ClassReferenceNode(this, opcode, type)
        instructions.add(classReference)
    }

    /**
     * TODO
     */
    override fun visitParameter(name: String?, access: Int) {
        super.visitParameter(name, access)
    }


    /**
     * Renames this method and all invocations of this method
     */
    suspend fun rename(newName: String) {
        val oldName = name
        this.name = newName
        References.methodNames.remove("${parent.name}.${oldName}.${desc}")
        References.methodNames["${parent.name}.${newName}.${desc}"] = this
        coroutineScope {
            this@ByteMethod.invocations.map {
                async {
                    it.name = newName
                    it.addToMethod()
                }
            }.awaitAll()
        }
    }

    /**
     * Removes this method as well as all invocations of this method in all scanned files
     */
    fun delete() {
        invocations.forEach {
            it.getMethod()?.instructions?.remove(it)
        }
        invocations.clear()
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

    /**
     * Returns true if this method is abstract
     */
    fun isAbstract() = Modifier.isAbstract(access)

    /**
     * Sets this method as abstract
     */
    fun setAbstract() {
        access = access.or(Modifier.ABSTRACT)
    }

    /**
     * Returns true if this method is public
     */
    fun isPublic() = Modifier.isPublic(access)

    /**
     * Sets this method as public
     */
    fun setPublic() {
        access = access.or(Modifier.PUBLIC)
        if (this.isPrivate()) {
            access = access.xor(Modifier.PRIVATE)
        }
    }

    /**
     * Returns true if this method is private
     */
    fun isPrivate() = Modifier.isPrivate(access)

    /**
     * Sets this method as a private method
     */
    fun setPrivate() {
        access = access.or(Modifier.PRIVATE)
        if (this.isPublic()) {
            access = access.xor(Modifier.PUBLIC)
        }
    }

    /**
     * Returns true if this method is static
     */
    fun isStatic() = Modifier.isStatic(access)

    /**
     * Makes this method static
     */
    fun setStatic() {
        access = access.or(Modifier.STATIC)
    }

    /**
     * Annotates this method's declaration.
     * @param name - the name of the Class of the annotation
     * @param fieldsToValues - Pairs of annotation Field names to their values
     * @sample - method.annotate("Generated", "methodName" to method.name)
     */
    open fun annotate(
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

    /**
     * Returns this methods control flow as a String in DOT format
     * @return - the CFG in DOT format
     */
    fun getCfgDot(): String {
        val writer = StringWriter()
        val exporter = DOTExporter<ByteBlockNode, DefaultEdge>(Function { t: ByteBlockNode ->
            t.toString().replace("-", "")
        })
        exporter.exportGraph(controlFlow, writer)
        return writer.toString()
    }

    fun drawFlowGraph(name: String = this.name): BufferedImage? {
        val adapter = JGraphXAdapter(controlFlow)
        val layout = mxCompactTreeLayout(adapter)
        layout.execute(adapter.defaultParent)
        val image =
            mxCellRenderer.createBufferedImage(layout.graph, null, 2.0, null, true, null)
        with(File("graphs")) {
            if (!exists())
                mkdir()
            if (image != null) {
                ImageIO.write(image, "PNG", File("graphs",
                    "${name.replace("<", "").replace(">", "")}.png"))
                return image
            }

        }
        log.error { "Error generating image for the CFG of $name" }
        return null
    }
}
