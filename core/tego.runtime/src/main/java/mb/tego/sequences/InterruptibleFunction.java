package mb.tego.sequences;

import java.util.function.Function;

/**
 * A function that accepts one argument and produces a result,
 * optionally throwing an {@link InterruptedException}.
 *
 * To write a function that is identity, write {@code x -> x}.
 *
 * @param <T> the type of the argument to this function
 * @param <R> the type of result
 */
@FunctionalInterface
public interface InterruptibleFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the argument
     * @return the result
     */
    R apply(final T t) throws InterruptedException;

    /**
     * Returns an {@link InterruptibleFunction} that performs
     * this operation followed by the specified operation.
     *
     * @param after the operation to perform after this operation
     * @param <V> the type of result
     * @return an {@link InterruptibleFunction} representing the composite of
     * this operation and the specified operation
     */
    default <V> InterruptibleFunction<T, V> andThen(InterruptibleFunction<? super R, ? extends V> after) {
        return (T t) -> after.apply(apply(t));
    }

    /**
     * Returns an {@link InterruptibleFunction} that performs
     * the specified operation follows by this operation.
     *
     * @param before the operation to perform before this operation
     * @param <V> the type of intermediate value
     * @return an {@link InterruptibleFunction} representing the composite of
     * the specified operation and this operation
     */
    default <V> InterruptibleFunction<V, R> compose(InterruptibleFunction<? super V, ? extends T> before) {
        return (V v) -> apply(before.apply(v));
    }

    /**
     * Wraps a {@link Function} into an {@link InterruptibleFunction}.
     *
     * @param function the function to wrap
     * @param <T> the type of the argument to this function
     * @param <R> the type of result
     * @return the wrapped function
     */
    static <T, R> InterruptibleFunction<T, R> from(Function<T, R> function) {
        //noinspection NullableProblems
        return function::apply;
    }
}
