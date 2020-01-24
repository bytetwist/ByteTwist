package org.bytetwist.bytetist.exceptions

/**
 * Thrown when the InputDir of a scanner hasn't been set before the scan method is called
 */
class NoInputDir : Exception() {
    /**
     * Initializes the *cause* of this throwable to the specified value.
     * (The cause is the throwable that caused this throwable to get thrown.)
     *
     *
     * This method can be called at most once.  It is generally called from
     * within the constructor, or immediately after creating the
     * throwable.  If this throwable was created
     * with [.Throwable] or
     * [.Throwable], this method cannot be called
     * even once.
     *
     *
     * An example of using this method on a legacy throwable type
     * without other support for setting the cause is:
     *
     * <pre>
     * try {
     * lowLevelOp();
     * } catch (LowLevelException le) {
     * throw (HighLevelException)
     * new HighLevelException().initCause(le); // Legacy constructor
     * }
    </pre> *
     *
     * @param  cause the cause (which is saved for later retrieval by the
     * [.getCause] method).  (A `null` value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     * @return  a reference to this `Throwable` instance.
     * @throws IllegalArgumentException if `cause` is this
     * throwable.  (A throwable cannot be its own cause.)
     * @throws IllegalStateException if this throwable was
     * created with [.Throwable] or
     * [.Throwable], or this method has already
     * been called on this throwable.
     * @since  1.4
     */
    override fun initCause(cause: Throwable?): Throwable {
        return super.initCause(cause)
    }
}