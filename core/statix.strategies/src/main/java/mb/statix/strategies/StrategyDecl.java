package mb.statix.strategies;

import mb.statix.sequences.Seq;
import mb.statix.strategies.runtime.TegoEngine;

/**
 * A strategy declaration.
 */
public interface StrategyDecl {

    /**
     * Gets the arity of the strategy.
     *
     * The arity of a basic strategy {@code T -> R} is 0.
     *
     * @return the arity of the strategy, excluding the input argument
     */
    int getArity();

    /**
     * Evaluates the strategy.
     *
     * This is a trampoline method.
     *
     * Do <i>not</i> call this method directly.
     * This method is intended for use
     * when a more specific and type-safe method cannot be found.
     *
     * @param engine the Tego engine
     * @param ctx the context
     * @param args the arguments
     * @param input the input argument
     * @return the lazy sequence of results; or an empty sequence if the strategy failed
     * @throws IllegalArgumentException if any of the arguments is of the wrong type;
     * if any of the arguments is {@code null}, if {@code args} has a number of elements
     * different from the strategy's arity
     */
    Seq<?> evalInternal(TegoEngine engine, Object ctx, Object[] args, Object input);

}
