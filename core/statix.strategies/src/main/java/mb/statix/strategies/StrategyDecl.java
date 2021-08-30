package mb.statix.strategies;

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

}
