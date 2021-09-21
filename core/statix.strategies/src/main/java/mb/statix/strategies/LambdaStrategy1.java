package mb.statix.strategies;

import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A lambda strategy that can be used to adapt a strategy with one argument.
 *
 * Use {@link StrategyExt#lam}.
 *
 * @param <CTX> the type of context (invariant)
 * @param <A1> the type of the first argument (contravariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface LambdaStrategy1<CTX, A1, T, R> extends Strategy1<CTX, A1, T, R> {
    @Override
    default @Nullable R evalInternal(TegoEngine engine, CTX ctx, A1 arg1, T input) {
        return engine.eval(apply(arg1), ctx, input);
    }

    @Override
    Strategy<CTX, T, R> apply(A1 arg1);
}
