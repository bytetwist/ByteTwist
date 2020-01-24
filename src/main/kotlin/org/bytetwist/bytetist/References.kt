package org.bytetwist.bytetist

import org.bytetwist.bytetist.nodes.CompiledClass
import org.bytetwist.bytetist.nodes.CompiledField
import org.bytetwist.bytetist.nodes.CompiledMethod
import java.util.concurrent.ConcurrentHashMap

object References {
    val classNames = ConcurrentHashMap<String, CompiledClass>()

    val fieldNames = ConcurrentHashMap<String, CompiledField>()

    val methodNames = ConcurrentHashMap<String, CompiledMethod>()

}