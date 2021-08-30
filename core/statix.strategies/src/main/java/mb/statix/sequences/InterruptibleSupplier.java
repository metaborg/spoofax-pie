package mb.statix.sequences;

/**
 * A function that produces a result,
 * optionally throwing an {@link InterruptedException}.
 *
 * @param <R> the type of result
 */
@FunctionalInterface
public interface InterruptibleSupplier<R> {

    /**
     * Applies this function.
     *
     * @return the result
     */
    R get() throws InterruptedException;

}
