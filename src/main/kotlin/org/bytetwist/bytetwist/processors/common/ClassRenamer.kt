package org.bytetwist.bytetwist.processors.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bytetwist.bytetwist.nodes.CompiledClass
import org.bytetwist.bytetwist.processors.AbstractNodeProcessor
import org.bytetwist.bytetwist.processors.log
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class ClassRenamer : AbstractNodeProcessor<CompiledClass>() {

    override val type: KClass<CompiledClass> = CompiledClass::class

    private val abstractProcessed = AtomicInteger()
    private val interfacesProcessed = AtomicInteger()


    override fun onComplete() {
        log.info { "Renamed $abstractProcessed abstract classes and $interfacesProcessed interfaces" }
        super.onComplete()
    }

    override fun preProcess(node: CompiledClass): Boolean {
        return node.name.length <= 3 || node.name.length > 60
    }

    override fun process(node: CompiledClass) {
        runBlocking {
            node.rename("Class${nodesProcessed}")
        }
    }
}