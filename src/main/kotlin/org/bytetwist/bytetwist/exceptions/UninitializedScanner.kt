package org.bytetwist.bytetwist.exceptions

import java.lang.Exception

/**
 * Thrown when the [org.bytetwist.bytetwist.processors.ProcessingQueue] has been instructed to run the processors before
 * the scanners.
 */
class UninitializedScanner : Exception() {
}