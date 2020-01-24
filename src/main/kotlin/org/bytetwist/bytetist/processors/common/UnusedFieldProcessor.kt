package org.bytetwist.bytetist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetist.nodes.CompiledField
import org.bytetwist.bytetist.processors.AbstractProcessor

class UnusedFieldProcessor : AbstractProcessor<CompiledField>() {
    override val type = CompiledField::class


    override fun process(node: CompiledField) {
        node.delete()
    }

    override fun preProcess(node: CompiledField): Boolean {
        return node.references.size == 0
    }
}