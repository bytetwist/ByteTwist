package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.CompiledClass
import org.bytetwist.bytetwist.nodes.CompiledField
import org.bytetwist.bytetwist.nodes.CompiledMethod
import org.bytetwist.bytetwist.processors.AbstractProcessor
import org.bytetwist.bytetwist.processors.log


@ExperimentalCoroutinesApi
class BasicClassProcessor : AbstractProcessor<CompiledClass>() {
     override val type = CompiledClass::class

    override fun preProcess(node: CompiledClass): Boolean {
        return node.methods.filterIsInstance(CompiledMethod::class.java).all { methodNode ->
            methodNode.isStatic() } &&
                node.fields.all { fieldNode -> (fieldNode as CompiledField).isStatic() }
    }

    override fun process(node: CompiledClass) {
        log.info { "Processing ${node.name}" }
    }
}