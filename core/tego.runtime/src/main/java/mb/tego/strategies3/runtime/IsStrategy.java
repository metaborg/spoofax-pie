package mb.tego.strategies3.runtime;

import mb.tego.sequences.Computation;
import mb.tego.sequences.Seq;
import mb.tego.strategies3.NamedStrategy1;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * Determines whether a value matches the expected type.
 *
 * @param <T> the type of input (contravariant)
 * @param <R> the type of output (covariant)
 */
public final class IsStrategy<T, R> extends NamedStrategy1<Class<R>, T, R> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final IsStrategy instance = new IsStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T, R> IsStrategy<T, R> getInstance() { return (IsStrategy<T, R>)instance; }

    private IsStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T, R> Seq<R> eval(TegoEngine engine, Class<R> cls, T input) {
        //noinspection unchecked
        return Computation.from(() -> cls.isAssignableFrom(input.getClass()) ? Optional.of((R)input) : Optional.empty());
    }

    @Override public Seq<R> evalInternal(TegoEngine engine, Class<R> cls, T input) {
        return eval(engine, cls, input);
    }

    @Override
    public String getName() {
        return "is";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "cls";
            default: return super.getParamName(index);
        }
    }

}
