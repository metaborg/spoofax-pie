package mb.tego.strategies3;


import mb.tego.sequences.Seq;
import mb.tego.strategies3.StrategyExt;
import mb.tego.strategies3.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A function strategy that can be used to adapt a function call with one argument as a strategy.
 * <p>
 * Use {@link StrategyExt#fun}.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface FunctionStrategy<T, R> extends Strategy<T, R> {
    @Override
    default Seq<R> evalInternal(TegoEngine engine, T input) {
        return call(input);
    }

    Seq<R> call(T input);
}
