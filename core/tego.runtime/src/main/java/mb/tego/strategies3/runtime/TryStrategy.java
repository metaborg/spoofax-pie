package mb.tego.strategies3.runtime;

import mb.tego.sequences.Seq;
import mb.tego.strategies3.NamedStrategy1;
import mb.tego.strategies3.Strategy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Try strategy.
 * <p>
 * This returns the results of the strategy, or the input if the strategy failed.
 *
 * @param <T> the type of input and output (invariant)
 */
public final class TryStrategy<T> extends NamedStrategy1<Strategy<T, T>, T, T> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final TryStrategy instance = new TryStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T> TryStrategy<T> getInstance() { return (TryStrategy<T>)instance; }

    private TryStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T> Seq<T> eval(TegoEngine engine, Strategy<T, T> s, T input) {
        // return <glc(s, id, id)> input
        //noinspection ConstantConditions
        return engine.eval(GlcStrategy.<T, T, T>getInstance().apply(
            s, (engine1, input1) -> Seq.of(input1), (engine2, input2) -> Seq.of(input2)
        ), input);
    }

    @Override
    public Seq<T> evalInternal(TegoEngine engine, Strategy<T, T> s, T input) {
        return eval(engine, s, input);
    }

    @Override
    public String getName() {
        return "try";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
