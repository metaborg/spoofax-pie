package mb.statix.patterns;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pattern.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of result (covariant)
 */
@FunctionalInterface
public interface Pattern<CTX, T, R> {

    /**
     * Matches the pattern.
     *
     * @param ctx the context
     * @param input the input argument
     * @return {@code true} if the pattern matches;
     * otherwise, {@code false}
     */
    boolean match(CTX ctx, @Nullable T input);

}
