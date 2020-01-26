package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.CompiledField
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor

@ExperimentalCoroutinesApi
class FieldRenamer : AbstractNodeProcessor<CompiledField>() {


    override fun process(node: CompiledField) {
        node.rename("field$nodesProcessed")
    }

    override fun preProcess(node: CompiledField): Boolean {
        return node.name.length <= 3 || node.name.length > 60
    }

    override val type = CompiledField::class
}