package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.sequences.SeqBase;
import mb.tego.strategies.NamedStrategy3;
import mb.tego.strategies.Strategy;
import mb.tego.utils.ExcludeFromJacocoGeneratedReport;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Conditional strategy.
 *
 * @param <T> the type of input (contravariant)
 * @param <U> the type of intermediate (invariant)
 * @param <R> the type of output (covariant)
 */
public final class IfStrategy<T, U, R> extends NamedStrategy3<Strategy<T, @Nullable U>, Strategy<U, @Nullable R>, Strategy<T, @Nullable R>, T, @Nullable R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final IfStrategy instance = new IfStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, U, R> IfStrategy<T, U, R> getInstance() { return (IfStrategy<T, U, R>)instance; }

    private IfStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, U, R> @Nullable R eval(
        TegoEngine engine,
        Strategy<T, @Nullable U> condition,
        Strategy<U, @Nullable R> onSuccess,
        Strategy<T, @Nullable R> onFailure,
        T input
    ) {
        final @Nullable U c = engine.eval(condition, input);
        if (c != null) {
            return engine.eval(onSuccess, c);
        } else {
            return engine.eval(onFailure, input);
        }
    }

    @Override
    public @Nullable R evalInternal(
        TegoEngine engine,
        Strategy<T, @Nullable U> condition,
        Strategy<U, @Nullable R> onSuccess,
        Strategy<T, @Nullable R> onFailure,
        T input
    ) {
        return eval(engine, condition, onSuccess, onFailure, input);
    }

    @Override
    public String getName() {
        return "glc";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "condition";
            case 1: return "onSuccess";
            case 2: return "onFailure";
            default: return super.getParamName(index);
        }
    }
}
