package mb.statix.strategies;

import mb.statix.sequences.Seq;
import mb.statix.strategies.runtime.TegoEngine;
import mb.statix.strategies.runtime.TegoRuntime;

/**
 * A strategy.
 *
 * The {@link #evalInternal} method can be executed at any time, but no actual evaluations should take place.
 * The {@link Seq} returned by the {@link #evalInternal} method is a lazy sequence that, when iterated, will
 * compute its results. Multiple iterations will cause multiple computations, but all implementations
 * of strategies should instantiate only one iterable for each sequence, therefore only iterate once.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
@FunctionalInterface
public interface Strategy<CTX, T, R> extends StrategyDecl, PrintableStrategy {

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
     * @param ctx the context
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    Seq<R> evalInternal(TegoEngine engine, CTX ctx, T input);

    @SuppressWarnings("unchecked")
    @Override
    default Seq<?> evalInternal(TegoEngine engine, Object ctx, Object[] args, Object input) {
        assert args.length == 0 : "Expected 0 arguments, got " + args.length + ".";
        return evalInternal(engine, (CTX)ctx, (T)input);
    }
}
