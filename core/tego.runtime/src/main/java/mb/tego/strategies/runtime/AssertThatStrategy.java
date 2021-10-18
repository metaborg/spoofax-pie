package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.NamedStrategy2;
import mb.tego.strategies.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Startegy that assert that a boolean holds.
 *
 * @param <T> the type of input (contravariant)
 */
public final class AssertThatStrategy<T> extends NamedStrategy1<Strategy<T, Boolean>, T, Seq<T>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final AssertThatStrategy instance = new AssertThatStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T> AssertThatStrategy<T> getInstance() { return (AssertThatStrategy<T>)instance; }

    private AssertThatStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T> Seq<T> eval(TegoEngine engine, Strategy<T, Boolean> predicate, T input) {
        final @Nullable Boolean success = engine.eval(predicate, input);
        if (success != null && success) {
            return Seq.of(input);
        } else {
            return Seq.of();
        }
    }

    @Override
    public Seq<T> evalInternal(TegoEngine engine, Strategy<T, Boolean> predicate, T input) {
        return eval(engine, predicate, input);
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
