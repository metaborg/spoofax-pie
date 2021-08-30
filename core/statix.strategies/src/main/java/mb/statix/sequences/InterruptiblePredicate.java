package mb.statix.sequences;

/**
 * A predicate functional interface that can be interrupted
 * (and throws an {@link InterruptedException} in this case).
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

}

