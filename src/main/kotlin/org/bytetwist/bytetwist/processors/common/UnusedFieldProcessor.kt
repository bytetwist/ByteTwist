package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.CompiledField
import org.bytetwist.bytetwist.processors.AbstractProcessor

class UnusedFieldProcessor : AbstractProcessor<CompiledField>() {
    override val type = CompiledField::class


    override fun process(node: CompiledField) {
        node.delete()
    }

    override fun preProcess(node: CompiledField): Boolean {
        return node.references.size == 0
    }
}