package org.bytetwist.bytetwist.scanners

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import org.bytetwist.bytetwist.nodes.ByteClass
import java.io.File

@InternalCoroutinesApi
abstract class Scanner(var inputDir: File) {


    /**
     * Scans the user submitted input location for any compiled class files
     */
    abstract fun scan(): Flow<ByteClass>

}