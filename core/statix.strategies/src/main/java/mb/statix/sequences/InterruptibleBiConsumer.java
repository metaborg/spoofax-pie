package mb.statix.sequences;

/**
 * A bi-consumer functional interface that can be interrupted
 * (and throws an {@link InterruptedException} in this case).
 *
 * @param <T> the type of the first argument to this function
 * @param <U> the type of the second argument to this function
 */
@FunctionalInterface
public interface InterruptibleBiConsumer<T, U> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @throws InterruptedException if the operation was interrupted
     */
    void accept(final T t, final U u) throws InterruptedException;

    /**
     * Returns an {@code InterruptibleBiConsumer} that performs
     * this operation followed by the specified operation.
     *
     * @param after the operation to perform after this operation
     * @return the {@code InterruptibleBiConsumer} representing the composite of
     * this operation and the specified operation
     */
    default InterruptibleBiConsumer<T, U> andThen(InterruptibleBiConsumer<? super T, ? super U> after) {
        return (T t, U u) -> { accept(t, u); after.accept(t, u); };
    }
}

