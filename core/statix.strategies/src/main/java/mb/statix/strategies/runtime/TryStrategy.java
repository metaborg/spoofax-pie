package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.Strategy;

/**
 * Try strategy.
 *
 * This returns the results of the strategy, or the input if the strategy failed.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input and output (invariant)
 */
public final class TryStrategy<CTX, T> extends NamedStrategy1<CTX, Strategy<CTX, T, T>, T, T> {

    @SuppressWarnings("rawtypes")
    private static final TryStrategy instance = new TryStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, T> TryStrategy<CTX, T> getInstance() { return (TryStrategy<CTX, T>)instance; }

    private TryStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<T> eval(CTX ctx, Strategy<CTX, T, T> s, T input) {
        // return <glc(s, id, id)> input
        return GlcStrategy.<CTX, T, T, T>getInstance().apply(
            s, IdStrategy.getInstance(), IdStrategy.getInstance()
        ).eval(ctx, input);
    }

    @Override
    public String getName() {
        return "try";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
