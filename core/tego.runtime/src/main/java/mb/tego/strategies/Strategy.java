package mb.tego.strategies;

import mb.tego.sequences.Seq;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A strategy.
 *
 * The {@link #evalInternal} method can be executed at any time, but no actual evaluations should take place.
 * The {@link Seq} returned by the {@link #evalInternal} method is a lazy sequence that, when iterated, will
 * compute its results. Multiple iterations will cause multiple computations, but all implementations
 * of strategies should instantiate only one iterable for each sequence, therefore only iterate once.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface Strategy<T, R> extends StrategyDecl, PrintableStrategy {

    @Override
    default int getArity() { return 0; }

    /**
     * Evaluates the strategy.
     *
     * Typically, do <i>not</i> call this method directly.
     * This method is intended for use by a {@link TegoRuntime} implementation.
     * Instead, obtain a {@link TegoRuntime} implementation and call the
     * appropriate {@link TegoRuntime#eval} method.
     *
     * @param engine the Tego engine
     * @param input the input argument
     * @return the result; or {@code null} if the strategy failed
     */
    @Nullable R evalInternal(TegoEngine engine, T input);

    @SuppressWarnings("unchecked")
    @Override
    default @Nullable Object evalInternal(TegoEngine engine, Object[] args, Object input) {
        assert args.length == 0 : "Expected 0 arguments, got " + args.length + ".";
        return evalInternal(engine, (T)input);
    }
}
