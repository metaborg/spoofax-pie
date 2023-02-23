package mb.tego.sequences;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Base class for computations that return at most one value.
 *
 * @param <T> the type of value in the computation (covariant)
 */
public abstract class Computation<T> implements Seq<T> {

    /** Whether a value was once computed. */
    private boolean computed = false;

    @Nullable private T current = null;

    @SuppressWarnings("ConstantConditions")
    @Override
    public final T getCurrent() {
        return this.current;
    }

    @Override
    public final boolean next() throws InterruptedException {
        if (computed) return false;
        computed = true;
        try {
            final Optional<T> result = compute();
            if (result.isPresent()) {
                this.current = result.get();
                return true;
            }
        } catch (NoSuchElementException ex) {
            // Not thrown, to match SeqBase behavior
        } catch (Throwable ex) {
            // NOTE: This may be an InterruptedException
            throw ex;
        }
        return false;
    }

    /**
     * Performs the computation.
     *
     * @return an optional of a result, or an empty optional
     * @throws InterruptedException if the computation was interrupted
     */
    protected abstract Optional<T> compute() throws InterruptedException;

    /**
     * Returns an empty lazy sequence.
     * <p>
     * This is an initial operation.
     *
     * @param <T> the type of values in the sequence (covariant)
     * @return the empty sequence
     */
    @SuppressWarnings("unchecked")
    public static <T> Seq<T> empty() {
        return EmptySeq.instance;
    }

    /**
     * Returns a computation that once produces the given value.
     *
     * @param value the value to produce
     * @return the computation
     * @param <T> the type of value in the computation (covariant)
     */
    public static <T> Computation<T> of(T value) {
        return Computation.from(() -> Optional.of(value));
    }

    /**
     * Returns a computation that once produces the given value.
     *
     * @param value the value to produce
     * @return the computation
     * @param <T> the type of value in the computation (covariant)
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") public static <T> Computation<T> ofOptional(Optional<T> value) {
        return Computation.from(() -> value);
    }

//    /**
//     * Returns a lazy computation of a value that results from calling the given supplier once.
//     * <p>
//     * This is an initial operation.
//     * <p>
//     * The computation returns one element unless the supplier throws an exception.
//     * Any thrown exception is propagated to the caller.
//     * If the supplier returns {@code null} or throws {@link NoSuchElementException},
//     * then the computation failed but no exception is thrown.
//     *
//     * @param supplier the supplier
//     * @param <T> the type of value being supplied (covariant)
//     * @return the sequence of one element
//     */
//    public static <T> Computation<T> from(Supplier<Optional<T>> supplier) {
//        return new Computation<T>() {
//            @Override
//            protected Optional<T> compute() {
//                return supplier.get();
//            }
//        };
//    }

    /**
     * Returns a lazy computation of a value that results from calling the given supplier once.
     * <p>
     * This is an initial operation.
     * <p>
     * The computation returns one element unless the supplier throws an exception.
     * Any thrown exception is propagated to the caller.
     * If the supplier returns {@code null} or throws {@link NoSuchElementException},
     * then the computation failed but no exception is thrown.
     *
     * @param supplier the supplier
     * @param <T> the type of value being supplied (covariant)
     * @return the sequence of one element
     */
    public static <T> Computation<T> from(InterruptibleSupplier<Optional<T>> supplier) {
        return new Computation<T>() {
            @Override
            protected Optional<T> compute() throws InterruptedException {
                return supplier.get();
            }
        };
    }

//    /**
//     * Returns a lazy computation of a value that results from calling the given supplier once.
//     * <p>
//     * This is an initial operation.
//     * <p>
//     * The computation returns one element unless the supplier throws an exception.
//     * Any thrown exception is propagated to the caller.
//     * If the supplier returns {@code null} or throws {@link NoSuchElementException},
//     * then the computation failed but no exception is thrown.
//     *
//     * @param supplier the supplier
//     * @param <T> the type of value being supplied (covariant)
//     * @return the sequence of one element
//     */
//    public static <T> Computation<T> fromNullable(Supplier<T> supplier) {
//        return new Computation<T>() {
//            @Override
//            protected Optional<T> compute() {
//                final T result = supplier.get();
//                if (result != null)
//                    return Optional.of(result);
//                else
//                    return Optional.empty();
//            }
//        };
//    }

    /**
     * Returns a lazy computation of a value that results from calling the given supplier once.
     * <p>
     * This is an initial operation.
     * <p>
     * The computation returns one element unless the supplier throws an exception.
     * Any thrown exception is propagated to the caller.
     * If the supplier returns {@code null} or throws {@link NoSuchElementException},
     * then the computation failed but no exception is thrown.
     *
     * @param supplier the supplier
     * @param <T> the type of value being supplied (covariant)
     * @return the sequence of one element
     */
    public static <T> Computation<T> fromNullable(InterruptibleSupplier<T> supplier) {
        return new Computation<T>() {
            @Override
            protected Optional<T> compute() throws InterruptedException {
                final T result = supplier.get();
                if (result != null)
                    return Optional.of(result);
                else
                    return Optional.empty();
            }
        };
    }
}
