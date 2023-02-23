package mb.tego.strategies3.runtime;

import mb.tego.sequences.Computation;
import mb.tego.sequences.Seq;
import mb.tego.strategies3.NamedStrategy1;
import mb.tego.strategies3.Strategy;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * where() strategy.
 * <p>
 * This returns the original input if the inner strategy succeeds; or {@code null} if the inner strategy failed.
 *
 * @param <T> the type of input and output (invariant)
 */
public final class WhereStrategy<T, R> extends NamedStrategy1<Strategy<T, R>, T, T> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final WhereStrategy instance = new WhereStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> WhereStrategy<T, R> getInstance() { return (WhereStrategy<T, R>)instance; }

    private WhereStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<T> eval(TegoEngine engine, Strategy<T, R> s, T input) {
        final Seq<R> r = engine.eval(s, input);
        return Computation.from(() -> r.next() ? Optional.of(input) : Optional.empty());
    }

    @Override
    public Seq<T> evalInternal(TegoEngine engine, Strategy<T, R> s, T input) {
        return eval(engine, s, input);
    }

    @Override
    public String getName() {
        return "where";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }
}
