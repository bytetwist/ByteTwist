package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.CompiledField
import org.bytetwist.bytetwist.processors.AbstractProcessor

@ExperimentalCoroutinesApi
class FieldRenamer : AbstractProcessor<CompiledField>() {


    override fun process(node: CompiledField) {
        node.rename("field$nodesProcessed")
    }

    override val type = CompiledField::class
}