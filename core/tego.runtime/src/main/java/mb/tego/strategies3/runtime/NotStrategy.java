package mb.tego.strategies3.runtime;

import mb.tego.sequences.Computation;
import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies3.NamedStrategy;
import mb.tego.strategies3.NamedStrategy1;
import mb.tego.strategies3.NamedStrategy2;
import mb.tego.strategies3.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * When the strategy succeeds, this strategy fails.
 * When the strategy fails, this strategy returns the original input.
 */
public final class NotStrategy<T, R> extends NamedStrategy1<Strategy<T, R>, T, T> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final NotStrategy instance = new NotStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> NotStrategy<T, R> getInstance() { return (NotStrategy<T, R>)instance; }

    private NotStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<T> eval(TegoEngine engine, Strategy<T, R> s, T input) {
        final Seq<R> r = engine.eval(s, input);
        return Computation.from(() -> r.next() ? Optional.empty() : Optional.of(input));
    }

    @Override
    public Seq<T> evalInternal(TegoEngine engine, Strategy<T, @Nullable R> s, T input) {
        return eval(engine, s, input);
    }

    @Override
    public String getName() {
        return "not";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
