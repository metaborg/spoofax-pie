package mb.tego.patterns;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pattern that matches a particular type.
 * <p>
 * Note: due to Java type erasure, this cannot deal with generics.
 * Therefore, {@code List<Integer>} and {@code List<String>} are considered the same.
 *
 * @param <R> the type of result (covariant)
 */
public final class TypeOfPattern<R> implements Pattern<Object, R> {

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
    public boolean match(@Nullable Object input) {
        return input != null && cls.isAssignableFrom(input.getClass());
    }

}
