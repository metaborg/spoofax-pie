package mb.statix.strategies.runtime;

import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.Strategy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * where() strategy.
 *
 * This returns the original input if the inner strategy succeeds; or {@code null} if the inner strategy failed.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input and output (invariant)
 */
public final class WhereStrategy<CTX, T, R> extends NamedStrategy1<CTX, Strategy<CTX, T, @Nullable R>, T, @Nullable T> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final WhereStrategy instance = new WhereStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <CTX, T, R> WhereStrategy<CTX, T, R> getInstance() { return (WhereStrategy<CTX, T, R>)instance; }

    private WhereStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <CTX, T, R> @Nullable T eval(TegoEngine engine, CTX ctx, Strategy<CTX, T, @Nullable R> s, T input) {
        final @Nullable R r = engine.eval(s, ctx, input);
        return r != null ? input : null;
    }

    @Override
    public @Nullable T evalInternal(TegoEngine engine, CTX ctx, Strategy<CTX, T, @Nullable R> s, T input) {
        return eval(engine, ctx, s, input);
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
