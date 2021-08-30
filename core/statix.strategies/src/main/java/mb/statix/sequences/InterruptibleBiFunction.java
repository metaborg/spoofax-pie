package mb.statix.sequences;

/**
 * A function that accepts two arguments and produces a result,
 * optionally throwing an {@link InterruptedException}.
 *
 * @param <T> the type of the first argument to this function
 * @param <T> the type of the second argument to this function
 * @param <R> the type of result
 */
@FunctionalInterface
public interface InterruptibleBiFunction<T, U, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first argument
     * @param u the second argument
     * @return the result
     */
    R apply(final T t, final U u) throws InterruptedException;

    /**
     * Returns an {@link InterruptibleBiFunction} that performs
     * this operation followed by the specified operation.
     *
     * @param after the operation to perform after this operation
     * @param <V> the type of result
     * @return an {@link InterruptibleBiFunction} representing the composite of
     * this operation and the specified operation
     */
    default <V> InterruptibleBiFunction<T, U, V> andThen(InterruptibleFunction<? super R, ? extends V> after) {
        return (T t, U u) -> after.apply(apply(t, u));
    }

}
