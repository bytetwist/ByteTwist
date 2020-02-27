package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.log
import kotlin.reflect.KClass



@ExperimentalCoroutinesApi
class JumpOptimizingProcessor : AbstractNodeProcessor<ByteMethod>() {
    override val type: KClass<ByteMethod> = ByteMethod::class

    /**
     * This method gets called after the processor has finished processing all of its nodes.
     * It can be overridden to perform any post-processing logic
     */
    override fun onComplete() {
        log.info { "Optimized $nodesProcessed Jumps in ${super.timer}" }
    }

    override fun process(node: ByteMethod) {
        for (it in node.instructions) {
            if (it is JumpInsnNode) {
                it.label
                var target: AbstractInsnNode = it.label
                while (target.opcode < 0) {
                    target = target.next
                }
                if (target.opcode == Opcodes.GOTO) {
                    it.label = (target as JumpInsnNode).label
                    nodesProcessed.getAndIncrement()
                }
            }
        }
    }
}