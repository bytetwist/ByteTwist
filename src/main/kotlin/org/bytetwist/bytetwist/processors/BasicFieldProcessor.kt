package org.bytetwist.bytetwist.processors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.FieldReferenceNode
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class BasicFieldProcessor : AbstractNodeProcessor<FieldReferenceNode>() {
     override val type: KClass<FieldReferenceNode>
        get() = FieldReferenceNode::class

    override fun preProcess(node: FieldReferenceNode): Boolean {
        return node.field() != null
    }

    override fun process(node: FieldReferenceNode) {
        if (!node.staticReference() && node.field()?.isStatic()!!) {
            log.info { "Static reference to non-static field ${node.field()?.name}" }
        }
    }
}