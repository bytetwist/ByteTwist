package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.ByteConstructor
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.objectweb.asm.Type
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.objectweb.asm.tree.analysis.BasicValue
import kotlin.reflect.KClass

class StaticFieldDeconstructor(override val type: KClass<ByteConstructor> = ByteConstructor::class) : AbstractNodeProcessor<ByteConstructor>() {

    private val analyzer = Analyzer<BasicValue>(BasicInterpreter())


    /**
     * Determines if this processor should process a node. If not overridden, the processor will process all
     * scanned objects of type [T]
     */
    override fun shouldProcess(node: ByteConstructor): Boolean {
        return node.name == "<clinit>" && node.fieldWrites().isNotEmpty()
    }

    /**
     * Processes each object that is accepted and passes [shouldProcess]. This is where the main code for
     * analysis/transformation, etc should go.
     */
    override fun process(node: ByteConstructor) {
        node.fieldWrites().filter { it.staticReference() && Type.getType(it.field()?.desc) in basicStaticTypes }
            .forEach {
                if (it.previous is LdcInsnNode) {
                    it.field()?.value = (it.previous as LdcInsnNode).cst
                    it.method.instructions.remove(it.previous)
                    it.method.instructions.remove(it)
                }
            }
    }

    companion object {
        private val basicStaticTypes = mutableListOf(Type.BYTE_TYPE, Type.CHAR_TYPE, Type.SHORT_TYPE, Type.INT_TYPE,
        Type.BOOLEAN_TYPE, Type.FLOAT_TYPE, Type.DOUBLE_TYPE, Type.LONG_TYPE)
    }
}