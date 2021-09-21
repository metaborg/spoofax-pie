package mb.statix.strategies;

import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A lambda strategy that can be used to adapt a strategy with one argument.
 *
 * Use {@link StrategyExt#lam}.
 *
 * @param <A1> the type of the first argument (contravariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface LambdaStrategy1<A1, T, R> extends Strategy1<A1, T, R> {
    @Override
    default @Nullable R evalInternal(TegoEngine engine, A1 arg1, T input) {
        return engine.eval(apply(arg1), input);
    }

    @Override
    Strategy<T, R> apply(A1 arg1);
}
