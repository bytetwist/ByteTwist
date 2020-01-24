package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytetwist.bytetwist.nodes.CompiledClass
import org.bytetwist.bytetwist.processors.AbstractProcessor
import org.bytetwist.bytetwist.processors.log
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
        log.info { node.typeReferences.size }
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