package mb.tego.strategies3;


import mb.tego.sequences.Seq;
import mb.tego.strategies3.StrategyExt;
import mb.tego.strategies3.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A lambda strategy that can be used to adapt a strategy with two arguments.
 * <p>
 * Use {@link StrategyExt#lam}.
 *
 * @param <A1> the type of the first argument (contravariant)
 * @param <A2> the type of the second argument (contravariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface LambdaStrategy2<A1, A2, T, R> extends Strategy2<A1, A2, T, R> {
    @Override
    default Seq<R> evalInternal(TegoEngine engine, A1 arg1, A2 arg2, T input) {
        return engine.eval(apply(arg1, arg2), input);
    }

    @Override
    Strategy<T, R> apply(A1 arg1, A2 arg2);
}
