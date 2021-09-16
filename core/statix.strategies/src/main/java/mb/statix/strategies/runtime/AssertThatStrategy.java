package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Startegy that assert that a boolean holds.
 *
 * @param <CTX> the type of context (invariant)
 * @param <T> the type of input (contravariant)
 */
public final class AssertThatStrategy<CTX, T> extends NamedStrategy1<CTX, Strategy<CTX, T, Boolean>, T, Seq<T>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final AssertThatStrategy instance = new AssertThatStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <CTX, T> AssertThatStrategy<CTX, T> getInstance() { return (AssertThatStrategy<CTX, T>)instance; }

    private AssertThatStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <CTX, T> Seq<T> eval(TegoEngine engine, CTX ctx, Strategy<CTX, T, Boolean> predicate, T input) {
        final @Nullable Boolean success = engine.eval(predicate, ctx, input);
        if (success != null && success) {
            return Seq.of(input);
        } else {
            return Seq.of();
        }
    }

    @Override
    public Seq<T> evalInternal(TegoEngine engine, CTX ctx, Strategy<CTX, T, Boolean> predicate, T input) {
        return eval(engine, ctx, predicate, input);
    }

    @Override
    public String getName() {
        return "assertThat";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "predicate";
            default: return super.getParamName(index);
        }
    }
}
