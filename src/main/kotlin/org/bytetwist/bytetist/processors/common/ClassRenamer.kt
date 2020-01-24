package org.bytetwist.bytetist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetist.nodes.CompiledClass
import org.bytetwist.bytetist.processors.AbstractProcessor
import org.bytetwist.bytetist.processors.log
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class ClassRenamer : AbstractProcessor<CompiledClass>() {

    override val type: KClass<CompiledClass> = CompiledClass::class

    private val abstractProcessed = AtomicInteger()
    private val interfacesProcessed = AtomicInteger()


    override fun onComplete() {
        log.info { "Renamed $abstractProcessed abstract classes and $interfacesProcessed interfaces" }
        super.onComplete()
    }

    override fun preProcess(node: CompiledClass): Boolean {
        return super.preProcess(node)
    }

    override fun process(node: CompiledClass) {
        if (node.isAbstract()) {
            node.rename("AbstractClass${abstractProcessed.getAndIncrement()}")
            return
        }
        if (node.isInterface()) {
            node.rename("Iface${interfacesProcessed.getAndIncrement()}")
            return
        }
        node.rename("Class${nodesProcessed}")
    }
}