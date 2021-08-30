package mb.statix.sequences;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A coroutine result.
 *
 * @param <T> the type of result (covariant)
 */
public final class CoroutineResult<T> {

    /**
     * Returns a coroutine result that represents success.
     *
     * @param value the resulting value
     * @param <T> the type of result
     * @return the successful coroutine result
     */
    public static <T> CoroutineResult<T> success(T value) {
        return new CoroutineResult<>(value);
    }

    /**
     * Returns a coroutine result that represents failure.
     *
     * @param <T> the type of result
     * @return the successful coroutine result
     */
    public static <T> CoroutineResult<T> failure(Throwable exception) {
        return new CoroutineResult<T>(new Failure(exception));
    }

    private final Object value;

    /**
     * Initializes a new instance of the {@link CoroutineResult} class.
     *
     * @param value the value of the result
     */
    private CoroutineResult(Object value) {
        this.value = value;
    }

    /**
     * Whether the result represents success.
     *
     * @return {@code true} when the result represents success;
     * otherwise, {@code false}
     */
    public boolean isSuccess() {
        return !(value instanceof Failure);
    }

    /**
     * Whether the result represents failure.
     *
     * @return {@code true} when the result represents failure;
     * otherwise, {@code false}
     */
    public boolean isFailure() {
        return value instanceof Failure;
    }

    /**
     * Gets the successful result; or {@code null} if there is none.
     *
     * @return the successful result; or {@code null} if there is none
     */
    @Nullable public T getOrNull() {
        if (isFailure()) return null;
        //noinspection unchecked
        return (T)this.value;
    }
    /**
     * Gets the failure exception; or {@code null} if there is none.
     *
     * @return the failure exception; or {@code null} if there is none
     */
    @Nullable public Throwable exceptionOrNull() {
        if (isSuccess()) return null;
        //noinspection unchecked
        return ((Failure)this.value).exception;
    }

    @Override public String toString() {
        if (this.value instanceof Failure) {
            return "Failure(" + ((Failure)this.value).exception + ")";
        } else {
            return "Success(" + this.value + ")";
        }
    }

    /**
     * Class used to represent a failed coroutine result.
     */
    private static final class Failure {
        /**
         * The exception that caused the failure.
         */
        public final Throwable exception;

        /**
         * Initializes a new instance of the {@link Failure} class.
         *
         * @param exception the exception that caused the failure
         */
        private Failure(Throwable exception) {
            this.exception = exception;
        }
    }
}
