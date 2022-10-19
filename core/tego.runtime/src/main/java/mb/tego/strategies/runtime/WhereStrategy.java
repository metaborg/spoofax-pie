package mb.tego.strategies.runtime;

import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.Strategy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * where() strategy.
 * <p>
 * This returns the original input if the inner strategy succeeds; or {@code null} if the inner strategy failed.
 *
 * @param <T> the type of input and output (invariant)
 */
public final class WhereStrategy<T, R> extends NamedStrategy1<Strategy<T, @Nullable R>, T, @Nullable T> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final WhereStrategy instance = new WhereStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> WhereStrategy<T, R> getInstance() { return (WhereStrategy<T, R>)instance; }

    private WhereStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> @Nullable T eval(TegoEngine engine, Strategy<T, @Nullable R> s, T input) {
        final @Nullable R r = engine.eval(s, input);
        return r != null ? input : null;
    }

    @Override
    public @Nullable T evalInternal(TegoEngine engine, Strategy<T, @Nullable R> s, T input) {
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
