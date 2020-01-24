package org.bytetwist.bytetist.nodes

import org.objectweb.asm.tree.AbstractInsnNode
import java.util.*

class CompiledBlockNode(val method: CompiledMethod, val startIndex: Int, var endIndex: Int = -1)

