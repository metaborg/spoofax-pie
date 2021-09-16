package mb.statix.strategies;

import mb.statix.sequences.Seq;
import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    default @Nullable R evalInternal(TegoEngine engine, CTX ctx, T input) {
        return call(input);
    }

    @Nullable R call(T input);
}
