package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy;
import mb.statix.strategies.NamedStrategy1;

/**
 * Constant strategy that discards the input and always returns the same value.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class ConstStrategy<CTX, T, R> extends NamedStrategy1<CTX, R, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ConstStrategy instance = new ConstStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <CTX, T, R> ConstStrategy<CTX, T, R> getInstance() { return (ConstStrategy<CTX, T, R>)instance; }

    private ConstStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <CTX, T, R> Seq<R> eval(TegoEngine engine, CTX ctx, R v, T input) {
        return Seq.of(v);
    }

    @Override public Seq<R> evalInternal(TegoEngine engine, CTX ctx, R v, T input) {
        return eval(engine, ctx, v, input);
    }

    @Override
    public String getName() {
        return "const";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "v";
            default: return super.getParamName(index);
        }
    }

}
