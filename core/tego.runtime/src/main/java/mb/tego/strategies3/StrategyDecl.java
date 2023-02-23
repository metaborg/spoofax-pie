package mb.tego.strategies3;


import mb.tego.sequences.Seq;
import mb.tego.strategies3.runtime.TegoEngine;
import mb.tego.utils.CaseFormat;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A strategy declaration.
 */
public interface StrategyDecl {

    /**
     * Gets the name of the strategy.
     * <p>
     * When the strategy is anonymous,
     * the return value of {@link #getName()} may not be human-readable.
     *
     * @return the name of the strategy
     */
    default String getName() {
        // Makes a best-effort to guess a printable name for the strategy.
        String strategyName = this.getClass().getSimpleName();
        // If the class name ends with Strategy (e.g., IdStrategy), remove this suffix.
        if (strategyName.endsWith("Strategy")) {
            strategyName = strategyName.substring(0, strategyName.length() - "Strategy".length());
        }
        // Translate the "CamelCase" name into a "kebab-case" name.
        return CaseFormat.combineKebabCase(CaseFormat.splitCamelCase(strategyName));
    }

    /**
     * Gets the arity of the strategy.
     * <p>
     * The arity of a basic strategy {@code T -> R} is 0.
     *
     * @return the arity of the strategy, excluding the input argument
     */
    int getArity();

    /**
     * Gets whether this strategy is anonymous.
     * <p>
     * A strategy is anonymous when it was created from a lambda or closure,
     * or when it is the application of a strategy.
     *
     * @return {@code true} when this strategy is anonymous;
     * otherwise, {@code false}
     */
    default boolean isAnonymous() { return true; }

    /**
     * Evaluates the strategy.
     * <p>
     * This is a trampoline method. Do <i>not</i> call this method directly.
     * This method is intended for use when a more specific and type-safe method cannot be found.
     *
     * @param engine the Tego engine
     * @param args the arguments
     * @param input the input argument
     * @return a lazy computation of multiple values
     * @throws IllegalArgumentException if any of the arguments is of the wrong type;
     * if any of the arguments is {@code null}, if {@code args} has a number of elements
     * different from the strategy's arity
     */
    @SuppressWarnings("rawtypes")
    Seq evalInternal(TegoEngine engine, Object[] args, Object input);

}
