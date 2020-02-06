package org.bytetwist.bytetwist.nodes

import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.TryCatchBlockNode

class ByteTryCatch(
        val method: ByteMethod,
        start: LabelNode,
        end: LabelNode,
        handler: LabelNode,
        type: String
) : TryCatchBlockNode(
        start,
        end,
        handler,
        type
), ByteNode {
}