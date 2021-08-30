package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy;

/**
 * Fail strategy.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class FailStrategy<CTX, T, R> extends NamedStrategy<CTX, T, R> {

    @SuppressWarnings("rawtypes")
    private static final FailStrategy instance = new FailStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, T, R> FailStrategy<CTX, T, R> getInstance() { return (FailStrategy<CTX, T, R>)instance; }

    private FailStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override public final Seq<R> eval(CTX ctx, T input) {
        return Seq.of();
    }

    @Override
    public String getName() {
        return "fail";
    }

    @Override
    public String getParamName(int index) {
        return super.getParamName(index);
    }

}
