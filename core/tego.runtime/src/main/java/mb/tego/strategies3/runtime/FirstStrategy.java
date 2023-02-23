package mb.tego.strategies3.runtime;

import mb.tego.sequences.Computation;
import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies3.NamedStrategy1;
import mb.tego.strategies3.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * first(s) strategy.
 * <p>
 * This returns the first result of the strategy if it returns one or more result, otherwise the strategy fails.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class FirstStrategy<T, R> extends NamedStrategy1<Strategy<T, R>, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final FirstStrategy instance = new FirstStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> FirstStrategy<T, R> getInstance() { return (FirstStrategy<T, R>)instance; }

    private FirstStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<R> eval(TegoEngine engine, Strategy<T, R> s, T input) {
        final Seq<R> seq = engine.eval(s, input);
        return Computation.from(() -> seq.next() ? Optional.of(seq.getCurrent()) : Optional.empty());
    }

    @Override
    public Seq<R> evalInternal(TegoEngine engine, Strategy<T, R> s, T input) {
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
