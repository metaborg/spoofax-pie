package mb.statix.sequences;

import java.util.function.BiPredicate;

/**
 * A predicate functional interface that can be interrupted
 * (and throws an {@link InterruptedException} in this case).
 *
 * @param <T> the type of the first argument to this function
 * @param <U> the type of the second argument to this function
 */
@FunctionalInterface
public interface InterruptibleBiPredicate<T, U> {

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @throws InterruptedException if the operation was interrupted
     */
    boolean test(final T t, final U u) throws InterruptedException;

}

