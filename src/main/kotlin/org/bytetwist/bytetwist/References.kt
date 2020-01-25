package org.bytetwist.bytetwist

import org.bytetwist.bytetwist.nodes.CompiledClass
import org.bytetwist.bytetwist.nodes.CompiledField
import org.bytetwist.bytetwist.nodes.CompiledMethod
import java.util.concurrent.ConcurrentHashMap

object References {
    val classNames = ConcurrentHashMap<String, CompiledClass>()

    val fieldNames = ConcurrentHashMap<String, CompiledField>()

    val methodNames = ConcurrentHashMap<String, CompiledMethod>()

    fun findMethod(name: String) : CompiledMethod? {
        return methodNames.values.find { c -> c.name == name }
    }

    fun findField(name: String) : CompiledField? {
        return fieldNames.values.find { f -> f.name == name }
    }

}