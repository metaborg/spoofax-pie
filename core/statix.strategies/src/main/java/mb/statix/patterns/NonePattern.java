package mb.statix.patterns;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pattern that matches nothing.
 *
 * @param <CTX> the type of context (invariant)
 * @param <R> the type of result (covariant)
 */
public final class NonePattern<CTX, R> implements Pattern<CTX, Object, R> {

    @SuppressWarnings("rawtypes")
    private static final NonePattern instance = new NonePattern();
    @SuppressWarnings("unchecked")
    public static <CTX, R> NonePattern<CTX, R> getInstance() { return (NonePattern<CTX, R>)instance; }

    private NonePattern() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public boolean match(CTX ctx, @Nullable Object input) {
        return false;
    }

}
