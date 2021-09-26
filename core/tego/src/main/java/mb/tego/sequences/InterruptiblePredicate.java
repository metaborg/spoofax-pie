package mb.tego.sequences;

import java.util.function.Predicate;

/**
 * A predicate functional interface that can be interrupted
 * (and throws an {@link InterruptedException} in this case).
 *
 * To write a predicate that is always true, write {@code x -> true}.
 * To write a predicate that is always false, write {@code x -> false}.
 *
 * @param <T> the type of the argument to this function
 */
@FunctionalInterface
public interface InterruptiblePredicate<T> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @throws InterruptedException if the operation was interrupted
     */
    boolean test(final T t) throws InterruptedException;

    /**
     * Wraps a {@link Predicate} into an {@link InterruptiblePredicate}.
     *
     * @param predicate the predicate to wrap
     * @param <T> the type of the argument to this function
     * @return the wrapped predicate
     */
    static <T> InterruptiblePredicate<T> from(Predicate<T> predicate) {
        return predicate::test;
    }
}

