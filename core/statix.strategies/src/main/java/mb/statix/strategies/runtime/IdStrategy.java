package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy;

/**
 * Identity strategy.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 */
public final class IdStrategy<CTX, T> extends NamedStrategy<CTX, T, T> {

    @SuppressWarnings("rawtypes")
    private static final IdStrategy instance = new IdStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, T> IdStrategy<CTX, T> getInstance() { return (IdStrategy<CTX, T>)instance; }

    private IdStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override public final Seq<T> eval(CTX ctx, T input) {
        return Seq.of(input);
    }

    @Override
    public String getName() {
        return "id";
    }

    @Override
    public String getParamName(int index) {
        return super.getParamName(index);
    }

}
