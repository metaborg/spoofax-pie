package mb.statix.sequences;

import java.util.function.Function;
import java.util.function.Supplier;

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

    /**
     * Wraps a {@link Supplier} into an {@link InterruptibleSupplier}.
     *
     * @param supplier the supplier to wrap
     * @param <R> the type of result
     * @return the wrapped supplier
     */
    static <R> InterruptibleSupplier<R> from(Supplier<R> supplier) {
        //noinspection NullableProblems
        return supplier::get;
    }
}
