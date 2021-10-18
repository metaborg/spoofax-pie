package mb.tego.functions;

import java.util.function.Function;

/**
 * A function that accepts three argument and produces a result.
 *
 * @param <T1> the type of the first argument
 * @param <T2> the type of the second argument
 * @param <T3> the type of the third argument
 * @param <R> the type of result
 */
@FunctionalInterface
public interface Function3<T1, T2, T3, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param p1 the first argument
     * @param p2 the second argument
     * @param p3 the third argument
     * @return the result
     */
    R apply(final T1 p1, final T2 p2, final T3 p3);

    /**
     * Returns an {@link Function3} that performs
     * this operation followed by the specified operation.
     *
     * @param after the operation to perform after this operation
     * @param <V> the type of result
     * @return an {@link Function3} representing the composite of
     * this operation and the specified operation
     */
    default <V> Function3<T1, T2, T3, V> andThen(Function<? super R, ? extends V> after) {
        return (T1 p1, T2 p2, T3 p3) -> after.apply(apply(p1, p2, p3));
    }

}
