package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Fail strategy.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
// TODO: Should take Object, should return @Nullable Void
public final class FailStrategy<T, R> extends NamedStrategy<T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final FailStrategy instance = new FailStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> FailStrategy<T, R> getInstance() { return (FailStrategy<T, R>)instance; }

    private FailStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> @Nullable R eval(TegoEngine engine, T input) {
        return null;
    }

    @Override public @Nullable R evalInternal(TegoEngine engine, T input) {
        return eval(engine, input);
    }

    @Override
    public String getName() {
        return "fail";
    }

    @Override
    public String getParamName(int index) {
        return super.getParamName(index);
    }

}
