package org.bytetwist.bytetist.processors

import com.google.common.collect.MultimapBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetist.nodes.CompiledMethod
import org.bytetwist.bytetist.nodes.ThrowExpressionNode
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class BasicMethodProcessor : AbstractProcessor<CompiledMethod>() {

    private val exceptionThrows = ArrayList<ThrowExpressionNode>()

     override val type: KClass<CompiledMethod>
        get() = CompiledMethod::class

    /**
     * This method gets called after the processor has finished processing all of its nodes.
     * It can be overridden to perform any post-processing logic
     */
    override fun onComplete() {
        log.info { "Found ${exceptionThrows.size} total Throw new Exception() expressions " }
        super.onComplete()
    }

    override fun preProcess(node: CompiledMethod): Boolean {
        return node.blocks.isNotEmpty()
    }

    override fun process(node: CompiledMethod) {
       // log.info { "Method ${node.name} has ${node.blocks.size} blocks" }
    }
}