package mb.statix.strategies;

import mb.statix.sequences.Seq;
import mb.statix.strategies.runtime.TegoEngine;

/**
 * An adapter strategy that can be used to adapt a function call with one argument as a strategy.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface AdapterStrategy<CTX, T, R> extends Strategy<CTX, T, R> {
    @Override
    default Seq<R> evalInternal(TegoEngine engine, CTX ctx, T input) {
        return call(input);
    }

    Seq<R> call(T input);
}
