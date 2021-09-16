package mb.statix.strategies;

import mb.statix.sequences.Seq;
import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An adapter strategy that can be used to adapt a function call with four arguments as a strategy.
 *
 * @param <CTX> the type of context (invariant)
 * @param <A1> the type of the first argument (contravariant)
 * @param <A2> the type of the second argument (contravariant)
 * @param <A3> the type of the third argument (contravariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface AdapterStrategy3<CTX, A1, A2, A3, T, R> extends Strategy3<CTX, A1, A2, A3, T, R> {
    @Override
    default @Nullable R evalInternal(TegoEngine engine, CTX ctx, A1 arg1, A2 arg2, A3 arg3, T input) {
        return call(arg1, arg2, arg3, input);
    }

    @Nullable R call(A1 arg1, A2 arg2, A3 arg3, T input);
}
