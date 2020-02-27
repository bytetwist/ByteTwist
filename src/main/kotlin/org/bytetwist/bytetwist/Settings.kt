package org.bytetwist.bytetwist

import org.bytetwist.bytetwist.nodes.ByteMethod
import org.bytetwist.bytetwist.nodes.ByteClass


class Settings {
    companion object {
        /**
         * Automatically annotates each [ByteMethod] with an annotation that has the number of Blocks and
         * control flow edges each method has. Useful for debugging and analysis
         */
        var annotateMethodComplexity = false

        /**
         * Automatically annotates each [ByteMethod] with an annotation that shows the number of
         * [org.bytetwist.bytetwist.nodes.ByteTryCatch]
         * each method has. Useful for debugging and analysis
        */
        var annotateTryCatchCount = false

        var annotateLocalVariables = false

        /**
         * Automatically annotates [ByteClass]es after they have been renamed or moved with an annotation that
         * has their old name or old package.
         */
        var annotateClassChanges = false

        /**
         * Number of threads the [org.bytetwist.bytetwist.scanners.Scanner] will use when scanning input directory
         * and then building the references and type hierarchy
         */
        var scannerThreads = 12

    }
}