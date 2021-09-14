package mb.statix.sequences;

import java.util.function.BiFunction;
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

    /**
     * Wraps a {@link BiPredicate} into an {@link InterruptibleBiPredicate}.
     *
     * @param predicate the predicate to wrap
     * @param <T> the type of the first argument to this function
     * @param <U> the type of the second argument to this function
     * @return the wrapped predicate
     */
    static <T, U> InterruptibleBiPredicate<T, U> from(BiPredicate<T, U> predicate) {
        return predicate::test;
    }

}

