package org.bytetwist.bytetist.processors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetist.nodes.CompiledField
import org.bytetwist.bytetist.nodes.FieldReferenceNode
import org.bytetwist.bytetist.nodes.FieldWrite
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class BasicFieldProcessor : AbstractProcessor<FieldReferenceNode>() {
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