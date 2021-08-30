package mb.statix.strategies;

import mb.statix.sequences.Seq;

/**
 * A strategy.
 *
 * The {@link #eval} method can be executed at any time, but no actual evaluations should take place.
 * The {@link Seq} returned by the {@link #eval} method is a lazy sequence that, when iterated, will
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
     * @param ctx the context
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     */
    Seq<R> eval(CTX ctx, T input);

}
