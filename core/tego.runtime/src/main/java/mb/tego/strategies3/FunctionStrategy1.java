package mb.tego.strategies3;


import mb.tego.sequences.Seq;
import mb.tego.strategies3.StrategyExt;
import mb.tego.strategies3.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A function strategy that can be used to adapt a function call with two arguments as a strategy.
 * <p>
 * Use {@link StrategyExt#fun}.
 *
 * @param <A1> the type of the first argument (contravariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface FunctionStrategy1<A1, T, R> extends Strategy1<A1, T, R> {
    @Override
    default Seq<R> evalInternal(TegoEngine engine, A1 arg1, T input) {
        return call(input, arg1);
    }

    Seq<R> call(T input, A1 arg1);
}
