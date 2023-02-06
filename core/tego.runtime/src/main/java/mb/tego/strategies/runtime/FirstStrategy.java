package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * first(s) strategy.
 * <p>
 * This returns the first result of the strategy if it returns one or more result, otherwise the strategy fails.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class FirstStrategy<T, R> extends NamedStrategy1<Strategy<T, Seq<R>>, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final FirstStrategy instance = new FirstStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> FirstStrategy<T, R> getInstance() { return (FirstStrategy<T, R>)instance; }

    private FirstStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> @Nullable R eval(TegoEngine engine, Strategy<T, Seq<R>> s, T input) {

        final @Nullable Seq<R> sSeq = engine.eval(s, input);
        try {
            if (sSeq != null && sSeq.next()) {
                return sSeq.getCurrent();
            } else {
                return null;
            }
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable R evalInternal(TegoEngine engine, Strategy<T, Seq<R>> s, T input) {
        return eval(engine, s, input);
    }

    @Override
    public String getName() {
        return "single";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
