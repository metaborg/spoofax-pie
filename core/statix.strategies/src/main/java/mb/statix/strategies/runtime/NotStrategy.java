package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * When the strategy succeeds, this strategy fails.
 * When the strategy fails, this strategy returns the original input.
 */
public final class NotStrategy<T, R> extends NamedStrategy1<Strategy<T, @Nullable R>, T, @Nullable T> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final NotStrategy instance = new NotStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> NotStrategy<T, R> getInstance() { return (NotStrategy<T, R>)instance; }

    private NotStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> @Nullable T eval(TegoEngine engine, Strategy<T, @Nullable R> s, T input) {
        final @Nullable R r = engine.eval(s, input);
        if (r == null) return input;
        else return null;
    }

    @Override
    public @Nullable T evalInternal(TegoEngine engine, Strategy<T, @Nullable R> s, T input) {
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
