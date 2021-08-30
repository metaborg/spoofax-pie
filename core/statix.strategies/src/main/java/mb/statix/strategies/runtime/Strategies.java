package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.Strategy;

import java.util.function.Function;

/**
 * Strategy convenience functions.
 */
public final class Strategies {
    private Strategies() { /* Prevent instantiation. */ }

    /**
     * Builds a recursive strategy.
     *
     * @param f the strategy builder function, which takes a reference to the built strategy itself
     * @param <CTX> the type of context (invariant)
     * @param <T> the type of input (contravariant)
     * @param <R> the type of output (covariant)
     * @return the resulting strategy
     */
    public static <CTX, T, R> Strategy<CTX, T, R> rec(Function<Strategy<CTX, T, R>, Strategy<CTX, T, R>> f) {
        return new Strategy<CTX, T, R>() {
            @Override
            public Seq<R> eval(CTX ctx, T input) {
                return f.apply(this).eval(ctx, input);
            }
        };
    }
}
