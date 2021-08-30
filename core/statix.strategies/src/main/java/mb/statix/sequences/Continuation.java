package mb.statix.sequences;

/**
 * A continuation.
 *
 * This represents a state and computation, which may or may not be resumed.
 *
 * @param <T> the type of return value (contravariant)
 */
public interface Continuation<T> {

    /**
     * Resumes the continuation with the given successful or failed result.
     *
     * A continuation cannot be resumed more than once.
     *
     * @param result the result to pass in
     */
    void resumeWith(CoroutineResult<T> result);

    /**
     * Resumes the continuation with the given result.
     *
     * A continuation cannot be resumed more than once.
     *
     * @param value the result to pass in
     */
    default void resume(T value) {
        resumeWith(CoroutineResult.success(value));
    }

    /**
     * Resumes the continuation with the given exception.
     *
     * A continuation cannot be resumed more than once.
     *
     * @param exception the exception to pass in
     */
    default void resumeWithException(Throwable exception) {
        resumeWith(CoroutineResult.failure(exception));
    }

}


