package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy;
import mb.statix.strategies.NamedStrategy1;

/**
 * Constant strategy that discards the input and always returns the same value.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class ConstStrategy<T, R> extends NamedStrategy1<R, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ConstStrategy instance = new ConstStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> ConstStrategy<T, R> getInstance() { return (ConstStrategy<T, R>)instance; }

    private ConstStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> R eval(TegoEngine engine, R v, T input) {
        return v;
    }

    @Override public R evalInternal(TegoEngine engine, R v, T input) {
        return eval(engine, v, input);
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
