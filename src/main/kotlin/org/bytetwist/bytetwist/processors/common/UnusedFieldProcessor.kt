package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor

class UnusedFieldProcessor : AbstractNodeProcessor<ByteField>() {
    override val type = ByteField::class


    override fun process(node: ByteField) {
        node.delete()
    }

    override fun preProcess(node: ByteField): Boolean {
        return node.references.size == 0
    }
}