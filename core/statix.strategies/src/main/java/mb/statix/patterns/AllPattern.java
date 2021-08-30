package mb.statix.patterns;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pattern that matches everything.
 *
 * @param <CTX> the type of context (invariant)
 */
public final class AllPattern<CTX, T> implements Pattern<CTX, T, T> {

    @SuppressWarnings("rawtypes")
    private static final AllPattern instance = new AllPattern();
    @SuppressWarnings("unchecked")
    public static <CTX, T> AllPattern<CTX, T> getInstance() { return (AllPattern<CTX, T>)instance; }

    private AllPattern() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public boolean match(CTX ctx, @Nullable T input) {
        return true;
    }

}
