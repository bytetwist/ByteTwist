package org.bytetwist.bytetist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.bytetwist.bytetist.nodes.CompiledMethod
import org.bytetwist.bytetist.processors.AbstractProcessor
import kotlin.reflect.KClass



@ExperimentalCoroutinesApi
class JumpOptimizingProcessor : AbstractProcessor<CompiledMethod>() {
    override val type: KClass<CompiledMethod> = CompiledMethod::class


    override fun process(node: CompiledMethod) {
        for (it in node.instructions) {
            if (it is JumpInsnNode) {
                var label = it.label
                var target: AbstractInsnNode = it.label
                while (true) {
                    while (!(target == null || target.opcode >= 0)) {
                        target = target.next
//                    target = (target.next as JumpInsnNode?)!!
                        nodesProcessed.getAndIncrement()
                    }
                    if (target.opcode == Opcodes.GOTO) {
                        label = (target as JumpInsnNode).label
                        nodesProcessed.getAndIncrement()
                    } else {
                        break
                    }
                }
                it.label = label
                if (it.opcode == Opcodes.GOTO) {
                    val op = target.opcode
                    if (op >= Opcodes.IRETURN && op <= Opcodes.RETURN || op == Opcodes.ATHROW) {
                        node.instructions.set(it, target.clone(null));
                    }
                }

            }
            nodesProcessed.getAndDecrement()
        }
    }
}