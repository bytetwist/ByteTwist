package org.bytetwist.bytetwist

import org.bytetwist.bytetwist.References.fieldNames
import org.bytetwist.bytetwist.nodes.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * Stores Maps of names to [org.bytetwist.bytetwist.nodes.ByteNode] objects.
 *
 * The String key of the map uses the same format that [org.objectweb.asm.commons.Remapper] uses.
 *
 */
object References {
    val concurrentHashMap = ConcurrentHashMap<String, ByteClass>()
    val classNames = concurrentHashMap

    val fieldNames = ConcurrentHashMap<String, ByteField>()

    val methodNames = ConcurrentHashMap<String, ByteMethod>()



    val blocks = CopyOnWriteArrayList<Block>()



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

/**
 * Finds a [ByteClass] from all scanned Classes based on name
 */
fun findClass(name: String): ByteClass? {
    return References.classNames[name]
}

/**
 * Finds a [ByteMethod] from all scanned Classes/methods based on name.
 */
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
 * Get's a [ByteMethod] with the specified name [methodName], so long as the method exists in the [ByteClass]
 */
fun ByteClass.getMethodByName(methodName: String) : ByteMethod? {
    return this.methods.associateBy { methodNode -> methodNode.name }[methodName] as ByteMethod?
}
