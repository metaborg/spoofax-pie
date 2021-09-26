package mb.tego.patterns;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pattern.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of result (covariant)
 */
@FunctionalInterface
public interface Pattern<T, R> {

    /**
     * Matches the pattern.
     *
     * @param input the input argument
     * @return {@code true} if the pattern matches;
     * otherwise, {@code false}
     */
    boolean match(@Nullable T input);

}
