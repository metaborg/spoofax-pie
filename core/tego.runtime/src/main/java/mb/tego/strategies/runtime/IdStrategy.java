package mb.tego.strategies.runtime;

import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy;

/**
 * Identity strategy.
 *
 * @param <T> the type of input (contravariant)
 */
public final class IdStrategy<T> extends NamedStrategy<T, T> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final IdStrategy instance = new IdStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <T> IdStrategy<T> getInstance() { return (IdStrategy<T>)instance; }

    private IdStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <T> T eval(TegoEngine engine, T input) {
        return input;
    }

    @Override public T evalInternal(TegoEngine engine, T input) {
        return eval(engine, input);
    }

    @Override
    public String getName() {
        return "id";
    }

    @Override
    public String getParamName(int index) {
        return super.getParamName(index);
    }

}
