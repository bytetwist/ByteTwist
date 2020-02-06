package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor

@ExperimentalCoroutinesApi
class FieldRenamer : AbstractNodeProcessor<ByteField>() {


    override fun process(node: ByteField) {
        node.annotate("Renamed", "oldName" to node.name)
        node.rename("field$nodesProcessed")
    }

    override fun preProcess(node: ByteField): Boolean {
        return node.name.length <= 3 || node.name.length > 60
    }

    override val type = ByteField::class
}