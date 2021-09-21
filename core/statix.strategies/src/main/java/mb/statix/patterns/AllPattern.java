package mb.statix.patterns;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pattern that matches everything.
 *
 */
public final class AllPattern<T> implements Pattern<T, T> {

    @SuppressWarnings("rawtypes")
    private static final AllPattern instance = new AllPattern();
    @SuppressWarnings("unchecked")
    public static <T> AllPattern<T> getInstance() { return (AllPattern<T>)instance; }

    private AllPattern() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public boolean match(@Nullable T input) {
        return true;
    }

}
