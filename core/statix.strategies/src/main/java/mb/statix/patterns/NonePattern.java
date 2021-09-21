package mb.statix.patterns;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pattern that matches nothing.
 *
 * @param <R> the type of result (covariant)
 */
public final class NonePattern<R> implements Pattern<Object, R> {

    @SuppressWarnings("rawtypes")
    private static final NonePattern instance = new NonePattern();
    @SuppressWarnings("unchecked")
    public static <R> NonePattern<R> getInstance() { return (NonePattern<R>)instance; }

    private NonePattern() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public boolean match(@Nullable Object input) {
        return false;
    }

}
