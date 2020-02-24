package org.bytetwist.bytetwist.nodes


import com.google.common.graph.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.bytetwist.bytetwist.References
import org.bytetwist.bytetwist.Settings
import org.bytetwist.bytetwist.processors.log
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultGraphType
import org.jgrapht.graph.DefaultGraphType.Builder
import org.jgrapht.graph.builder.GraphTypeBuilder.directed
import org.jgrapht.graph.guava.BaseNetworkAdapter
import org.jgrapht.graph.guava.MutableGraphAdapter
import org.jgrapht.graph.guava.MutableNetworkAdapter
import org.jgrapht.io.ComponentNameProvider
import org.jgrapht.nio.GraphExporter
import org.jgrapht.nio.IntegerIdProvider
import org.jgrapht.nio.dot.DOTExporter
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.SOURCE_MASK
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.AnalyzerException
import org.objectweb.asm.tree.analysis.BasicValue
import org.objectweb.asm.tree.analysis.BasicVerifier
import java.io.StringWriter
import java.lang.reflect.Modifier
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Function


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


    override fun visitVarInsn(opcode: Int, `var`: Int) {
        val reference = ByteVariableReference(opcode, `var`)
        instructions.add(reference)
    }

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

    fun typeReferences() = instructions.filterIsInstance(ClassReferenceNode::class.java)


    init {
        visibleAnnotations = mutableListOf<AnnotationNode>()
        invisibleAnnotations = mutableListOf<AnnotationNode>()
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
        val refs = instructions.filterIsInstance(ByteVariableReference::class.java).filter {
            it.`var` == index
        }
        if (!refs.isNullOrEmpty()) {
            refs.onEach { byteVar.references.add(it) }
        }
        super.visitLocalVariable(name, descriptor, signature, start, end, index)
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
        val annotationNode = ByteAnnotation(this, descriptor)
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
    suspend fun buildBlocks() {
        //   coroutineScope {
        // Sort try catch blocks
        //this.accept(TryCatchBlockSorter(this, access, name, desc, signature, exceptions.toTypedArray()))
        var block = ByteBlockNode(this@ByteMethod)
        for (insnNode in instructions) {
            when (insnNode) {
                instructions.first -> {
                    block = ByteBlockNode(this@ByteMethod)
                    block.add(insnNode)
                }
                is JumpInsnNode -> {
                    block.add(insnNode)
                    blocks.add(block)
                    val oldBlock = block.clone()
                    block = ByteBlockNode(this@ByteMethod)
                }
                else -> {
                    block.add(insnNode)
                    if (insnNode == instructions.last) {
                        blocks.add(block)
                    }
                }
            }
        }
        if (!this@ByteMethod.tryCatchBlocks.isNullOrEmpty()) {
            this@ByteMethod.tryCatchBlocks.onEach { tryCatchBlock ->
                //     async {
                if (tryCatchBlock != null) {
                    (tryCatchBlock as ByteTryCatch).buildMethodBlocks()
                }
                //   }
            }
        }

        try {
            val frames = analyzer.analyze(this@ByteMethod.parent.name, this@ByteMethod)
            instructions.zip(frames).forEach {
                if (it.second == null) {
                    log.debug { ";)" }
                }
            }
            if (Settings.annotateMethodComplexity) {
                //   launch {
                annotate("Complexity", "Blocks" to blocks.size, "Edges" to blocks.flatMap { it.edges }.count())
                // }
            }

            if (Settings.annotateTryCatchCount) {
                //   launch {
                annotate("TryCatchs", "number" to tryCatchBlocks.size)
                // }
            }

        } catch (e: AnalyzerException) {
            //log.error { e.message + " ${e.node} + ${e.node.opcode}" }
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

    // }

    /**
     * Do nothing, we don't want to keep line numbers
     */
    override fun visitLineNumber(line: Int, start: Label?) {

    }

    val cfg: MutableNetwork<ByteBlockNode, Any> =
            NetworkBuilder
                    .directed()
                    .allowsSelfLoops(true)
                    .allowsParallelEdges(true)
                    .build()

    /**
     * An Analyzer that builds a cfg for this [ByteMethod] and adds the edges to all of this methods blocks.
     *
     */
    private val analyzer = object : Analyzer<BasicValue>(BasicVerifier()) {

        /**
         * Adds a control flow edge from one block to another
         */
        override fun newControlFlowEdge(insnIndex: Int, successorIndex: Int) {
            val firstB = findBlock(instructions[insnIndex])
            val second = findBlock(instructions[successorIndex])
            if (firstB != null) {
                if (second != null) {
                    cfg.addEdge(firstB, second, DefaultEdge())
                }
            } else {
                val first = findBlockByLast(instructions[insnIndex])
                if (second != null) {
                    cfg.addEdge(first, second, DefaultEdge())
                }
            }
//            findBlockByLast(instructions[insnIndex])?.let {
//                val b = findBlock(instructions[successorIndex])
//                if (it.edges.add((b to EdgeDirection.OUT) as Pair<ByteBlockNode, EdgeDirection>)) {//                   // it.edges.add(b to EdgeDirection.IN)
//                }
//            }
        }

        override fun newControlFlowExceptionEdge(insnIndex: Int, successorIndex: Int): Boolean {
            val first = findBlock(instructions[insnIndex])
            val second = findBlock(instructions[successorIndex])
            if (first != null) {
                if (second != null) {
                    cfg.addEdge(first, second, DefaultEdge())
                }
            }
            return true
        }
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
     * Visists a type instruction to create a [ClassReferenceNode] and adds it to the [instructions]
     */
    override fun visitTypeInsn(opcode: Int, type: String?) {
        val classReference = ClassReferenceNode(this, opcode, type)
        instructions.add(classReference)
        References.typeReferences.add(classReference)
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

    fun printCfgDot() {
       val writer = StringWriter()
        val graph: MutableNetworkAdapter<ByteBlockNode, Any> = MutableNetworkAdapter(cfg)
        val exporter = DOTExporter<ByteBlockNode, String>(Function { t: ByteBlockNode ->
            t.toString().replace("-", "") })
        exporter.exportGraph(graph as Graph<ByteBlockNode, String>, writer)
        //log.info { writer.toString() }
    }
}