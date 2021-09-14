package mb.statix.sequences;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A consumer functional interface that can be interrupted
 * (and throws an {@link InterruptedException} in this case).
 *
 * To write a consumer that does nothing, write {@code x -> {}}.
 *
 * @param <T> the type of the argument to this function
 */
@FunctionalInterface
public interface InterruptibleConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws InterruptedException if the operation was interrupted
     */
    void accept(final T t) throws InterruptedException;

    /**
     * Returns an {@code InterruptibleConsumer} that performs
     * this operation followed by the specified operation.
     *
     * @param after the operation to perform after this operation
     * @return the {@code InterruptibleConsumer} representing the composite of
     * this operation and the specified operation
     */
    default InterruptibleConsumer<T> andThen(InterruptibleConsumer<? super T> after) {
        return (T t) -> { accept(t); after.accept(t); };
    }

    /**
     * Wraps a {@link Consumer} into an {@link InterruptibleConsumer}.
     *
     * @param consumer the consumer to wrap
     * @param <T> the type of the first argument to this function
     * @return the wrapped consumer
     */
    static <T> InterruptibleConsumer<T> from(Consumer<T> consumer) {
        return consumer::accept;
    }
}

