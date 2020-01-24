package org.bytetwist.bytetist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetist.nodes.CompiledField
import org.bytetwist.bytetist.processors.AbstractProcessor

@ExperimentalCoroutinesApi
class FieldRenamer : AbstractProcessor<CompiledField>() {


    override fun process(node: CompiledField) {
        node.rename("field$nodesProcessed")
    }

    override val type = CompiledField::class
}