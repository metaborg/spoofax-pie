package mb.tego.strategies;

import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A lambda strategy that can be used to adapt a strategy with three arguments.
 *
 * Use {@link StrategyExt#lam}.
 *
 * @param <A1> the type of the first argument (contravariant)
 * @param <A2> the type of the second argument (contravariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface LambdaStrategy3<A1, A2, A3, T, R> extends Strategy3<A1, A2, A3, T, R> {
    @Override
    default @Nullable R evalInternal(TegoEngine engine, A1 arg1, A2 arg2, A3 arg3, T input) {
        return engine.eval(apply(arg1, arg2, arg3), input);
    }

    @Override
    Strategy<T, R> apply(A1 arg1, A2 arg2, A3 arg3);
}
