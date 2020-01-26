package org.bytetwist.bytetwist

import org.bytetwist.bytetwist.nodes.CompiledClass
import org.bytetwist.bytetwist.nodes.CompiledField
import org.bytetwist.bytetwist.nodes.CompiledMethod
import java.util.concurrent.ConcurrentHashMap

/**
 * Stores Maps of names to [CompiledNode] objects.
 *
 * The String key of the map uses the same format that [org.objectweb.asm.commons.Remapper] uses.
 *
 */
object References {
    val classNames = ConcurrentHashMap<String, CompiledClass>()

    val fieldNames = ConcurrentHashMap<String, CompiledField>()

    val methodNames = ConcurrentHashMap<String, CompiledMethod>()

    /**
     * Finds a [CompiledMethod] by the [CompiledMethod.name] value.
     * Useful when you don't have a reference to the owner or the descriptor/signature
     */
    fun findMethod(name: String) : CompiledMethod? {
        return methodNames.values.find { c -> c.name == name }
    }

    /**
     * Finds a [CompiledField] by the [CompiledField.name] value.
     * Useful when you don't have a reference to the owner
     */
    fun findField(name: String) : CompiledField? {
        return fieldNames.values.find { f -> f.name == name }
    }

}