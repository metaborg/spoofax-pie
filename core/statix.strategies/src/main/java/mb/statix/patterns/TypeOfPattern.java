package mb.statix.patterns;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pattern that matches a particular type.
 *
 * Note: due to Java type erasure, this cannot deal with generics.
 * Therefore, {@code List<Integer>} and {@code List<String>} are considered the same.
 *
 * @param <CTX> the type of context (invariant)
 * @param <R> the type of result (covariant)
 */
public final class TypeOfPattern<CTX, R> implements Pattern<CTX, Object, R> {

    private final Class<? extends R> cls;

    /**
     * Initializes a new instance of the {@link TypeOfPattern} class.
     *
     * @param cls the class
     */
    public TypeOfPattern(Class<? extends R> cls) {
        this.cls = cls;
    }

    @Override
    public boolean match(CTX ctx, @Nullable Object input) {
        return input != null && cls.isAssignableFrom(input.getClass());
    }

}
