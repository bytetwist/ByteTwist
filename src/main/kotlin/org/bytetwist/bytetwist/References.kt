package org.bytetwist.bytetwist

import org.bytetwist.bytetwist.References.fieldNames
import org.bytetwist.bytetwist.nodes.ByteClass
import org.bytetwist.bytetwist.nodes.ByteField
import org.bytetwist.bytetwist.nodes.ByteMethod
import java.util.concurrent.ConcurrentHashMap

/**
 * Stores Maps of names to [CompiledNode] objects.
 *
 * The String key of the map uses the same format that [org.objectweb.asm.commons.Remapper] uses.
 *
 */
object References {
    val classNames = ConcurrentHashMap<String, ByteClass>()

    val fieldNames = ConcurrentHashMap<String, ByteField>()

    val methodNames = ConcurrentHashMap<String, ByteMethod>()

    fun findClass(name: String): ByteClass? {
        return classNames[name]
    }

    /**
     * Finds a [ByteMethod] by the [ByteMethod.name] value.
     * Useful when you don't have a reference to the owner or the descriptor/signature
     */
    fun findMethod(name: String) : ByteMethod? {
        return methodNames.values.find { c -> c.name == name }
    }

    fun findField(name: String) : ByteField? {
        return fieldNames.values.find { f -> f.name == name }
    }

}

inline fun findClass(name: String): ByteClass? {
    return References.classNames[name]
}

fun findMethod(name: String) : ByteMethod? {
    return References.methodNames.values.find { c -> c.name == name }
}

/**
 * Finds a [ByteField] by the [ByteField.name] value.
 * Useful when you don't have a reference to the owner
 */
fun findField(name: String) : ByteField? {
    return fieldNames.values.find { f -> f.name == name }
}

/**
 * Get's a method from a
 */
fun ByteClass.getMethodByName(methodName: String) : ByteMethod? {
    return this.methods.associateBy { methodNode -> methodNode.name }[methodName] as ByteMethod
}
