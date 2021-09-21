package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.Strategy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Returns a nullable as a list: either a singleton list if the value is non-null,
 * or an empty list if the value is null.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input/output (invariant)
 */
public final class NullableToListStrategy<CTX, T, R> extends NamedStrategy1<CTX, Strategy<CTX, T, @Nullable R>, T, Seq<R>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final NullableToListStrategy instance = new NullableToListStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <CTX, T, R> NullableToListStrategy<CTX, T, R> getInstance() { return (NullableToListStrategy<CTX, T, R>)instance; }

    private NullableToListStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <CTX, T, R> Seq<R> eval(TegoEngine engine, CTX ctx, Strategy<CTX, T, @Nullable R> s, T input) {
        final @Nullable R r = engine.eval(s, ctx, input);
        if (r == null) return Seq.of();
        return Seq.of(r);
    }

    @Override
    public Seq<R> evalInternal(TegoEngine engine, CTX ctx, Strategy<CTX, T, @Nullable R> s, T input) {
        return eval(engine, ctx, s, input);
    }

    @Override
    public String getName() {
        return "ntl";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "s";
            default: return super.getParamName(index);
        }
    }

}
