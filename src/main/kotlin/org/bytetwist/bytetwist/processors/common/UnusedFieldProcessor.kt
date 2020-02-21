package org.bytetwist.bytetwist.processors.common

import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor

/**
 * A basic/common processor that deletes ([ByteField.delete]) all fields that don't have any references
 */
class UnusedFieldProcessor : AbstractNodeProcessor<ByteField>() {
    override val type = ByteField::class

    override fun process(node: ByteField) {
        node.delete()
    }

    override fun shouldProcess(node: ByteField): Boolean {
        return node.references.size == 0
    }
}