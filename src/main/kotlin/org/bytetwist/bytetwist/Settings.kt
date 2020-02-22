package org.bytetwist.bytetwist

import org.bytetwist.bytetwist.nodes.ByteMethod


class Settings {
    companion object {
        /**
         * Automatically annotates each [ByteMethod] with an annotation that has the number of Blocks and
         * control flow edges each method has. Useful for debugging and analysis
         */
        var annotateMethodComplexity = false

        /**
         * Automatically annotates each [ByteMethod] with an annotation that shows the number of [TryCatchBlock]
         * each method has. Useful for debugging and analysis
        */
        var annotateTryCatchCount = false

        var annotateLocalVariables = false

    }
}