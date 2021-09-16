package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.Strategy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Try strategy.
 *
 * This returns the results of the strategy, or the input if the strategy failed.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input and output (invariant)
 */
public final class TryStrategy<CTX, T> extends NamedStrategy1<CTX, Strategy<CTX, T, Seq<T>>, T, Seq<T>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final TryStrategy instance = new TryStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <CTX, T> TryStrategy<CTX, T> getInstance() { return (TryStrategy<CTX, T>)instance; }

    private TryStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <CTX, T> Seq<T> eval(TegoEngine engine, CTX ctx, Strategy<CTX, T, Seq<T>> s, T input) {
        // return <glc(s, id, id)> input
        //noinspection ConstantConditions
        return engine.eval(GlcStrategy.<CTX, T, T, T>getInstance().apply(
            s, (engine1, ctx1, input1) -> Seq.of(input1), (engine2, ctx2, input2) -> Seq.of(input2)
        ), ctx, input);
    }

    @Override
    public @Nullable Seq<T> evalInternal(TegoEngine engine, CTX ctx, Strategy<CTX, T, Seq<T>> s, T input) {
        return eval(engine, ctx, s, input);
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
