package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.Strategy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Returns a nullable as a list: either a singleton list if the value is non-null,
 * or an empty list if the value is null.
 *
 * @param <T> the type of input/output (invariant)
 */
public final class NullableToListStrategy<T, R> extends NamedStrategy1<Strategy<T, @Nullable R>, T, Seq<R>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final NullableToListStrategy instance = new NullableToListStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> NullableToListStrategy<T, R> getInstance() { return (NullableToListStrategy<T, R>)instance; }

    private NullableToListStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<R> eval(TegoEngine engine, Strategy<T, @Nullable R> s, T input) {
        final @Nullable R r = engine.eval(s, input);
        if (r == null) return Seq.of();
        return Seq.of(r);
    }

    @Override
    public Seq<R> evalInternal(TegoEngine engine, Strategy<T, @Nullable R> s, T input) {
        return eval(engine, s, input);
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
