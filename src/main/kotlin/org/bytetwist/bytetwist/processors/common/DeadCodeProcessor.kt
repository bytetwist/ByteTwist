package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.log
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

class DeadCodeProcessor : AbstractNodeProcessor<ByteMethod>() {
    override val type: KClass<ByteMethod>
        get() = ByteMethod::class

    private val deadInstructions = AtomicInteger()

    override fun process(node: ByteMethod) {
        val interpreter = BasicInterpreter()
        val analyzer = Analyzer(interpreter)
        val frames = analyzer.analyze(node.parent.name, node)
        val instructions = node.instructions
        instructions.forEachIndexed { i, insnNode ->
            if (insnNode !is LabelNode && frames[i] == null) {
                deadInstructions.incrementAndGet()
                instructions.remove(insnNode)
            }
        }
    }

    /**
     * This method gets called after the processor has finished processing all of its nodes.
     * It can be overridden to perform any post-processing logic
     */
    override fun onComplete() {
        log.info { "Removed $deadInstructions dead instructions in $timer" }
    }

    override fun shouldProcess(node: ByteMethod): Boolean {
        return super.shouldProcess(node)
    }
}