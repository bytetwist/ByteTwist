package org.bytetwist.bytetist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetist.nodes.CompiledClass
import org.bytetwist.bytetist.nodes.CompiledField
import org.bytetwist.bytetist.nodes.CompiledMethod
import org.bytetwist.bytetist.processors.AbstractProcessor
import org.bytetwist.bytetist.processors.log


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