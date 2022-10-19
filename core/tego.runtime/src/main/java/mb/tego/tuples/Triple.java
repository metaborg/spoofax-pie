package mb.tego.tuples;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable tuple with three components.
 *
 * @param <T1> the type of the first component (contravariant)
 * @param <T2> the type of the second component (contravariant)
 * @param <T3> the type of the third component (contravariant)
 */
public final class Triple<T1, T2, T3> implements Tuple, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link Triple} from the specified components.
     *
     * @param component1 the first component
     * @param component2 the second component
     * @param component3 the third component
     * @param <T1> the type of the first component (contravariant)
     * @param <T2> the type of the second component (contravariant)
     * @param <T3> the type of the third component (contravariant)
     * @return the resulting tuple
     */
    public static <T1, T2, T3> Triple<T1, T2, T3> of(T1 component1, T2 component2, T3 component3) {
        return new Triple<>(component1, component2, component3);
    }

    /**
     * Creates a tuple from the specified iterable.
     * <p>
     * This shadows the {@link Tuple#from} method.
     *
     * @param components the components in the tuple
     * @return the tuple
     */
    public static <T> Triple<T, T, T> from(Iterable<T> components) {
        final Iterator<T> iterator = components.iterator();
        if (!iterator.hasNext()) throw new IllegalArgumentException("Expected iterable with 3 elements, found 0.");
        final T c1 = iterator.next();
        if (!iterator.hasNext()) throw new IllegalArgumentException("Expected iterable with 3 elements, found 1.");
        final T c2 = iterator.next();
        if (!iterator.hasNext()) throw new IllegalArgumentException("Expected iterable with 3 elements, found 2.");
        final T c3 = iterator.next();
        if (iterator.hasNext()) throw new IllegalArgumentException("Expected iterable with 3 elements, found more.");

        return new Triple<>(c1, c2, c3);
    }

    private final T1 component1;
    private final T2 component2;
    private final T3 component3;

    /**
     * Initializes a new instance of the {@link Triple} class.
     *
     * @param component1 the first component in the tuple
     * @param component2 the second component in the tuple
     * @param component3 the third component in the tuple
     */
    public Triple(T1 component1, T2 component2, T3 component3) {
        this.component1 = component1;
        this.component2 = component2;
        this.component3 = component3;
    }

    /**
     * Gets the first component in the tuple.
     * <p>
     * This method is provided for compatibility with Kotlin destructuring declarations.
     *
     * @return the first component in the tuple (which may be {@code null})
     */
    public T1 component1() {
        return component1;
    }

    /**
     * Gets the second component in the tuple.
     * <p>
     * This method is provided for compatibility with Kotlin destructuring declarations.
     *
     * @return the second component in the tuple (which may be {@code null})
     */
    public T2 component2() {
        return component2;
    }

    /**
     * Gets the third component in the tuple.
     * <p>
     * This method is provided for compatibility with Kotlin destructuring declarations.
     *
     * @return the third component in the tuple (which may be {@code null})
     */
    public T3 component3() {
        return component3;
    }

    @Override
    public int getArity() {
        return 3;
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case 0: return component1;
            case 1: return component2;
            case 2: return component3;
            default: throw new IndexOutOfBoundsException("'index' must be between 0 and " + getArity() + " (exclusive).");
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Triple)) return false;
        Triple<?, ?, ?> that = (Triple<?, ?, ?>)o;
        // We use `Objects.equals()` because the items might be `null`
        return Objects.equals(this.component1, that.component1)
            && Objects.equals(this.component2, that.component2)
            && Objects.equals(this.component3, that.component3);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + (component1 != null ? component1.hashCode() : 0);
        hash = 31 * hash + (component2 != null ? component2.hashCode() : 0);
        hash = 31 * hash + (component3 != null ? component3.hashCode() : 0);
        return hash;
    }

    @Override public String toString() {
        // This will likely be optimized by the compiler into calls to a StringBuilder or similar.
        return "(" + component1 + ", " + component2 + ", " + component3 + ')';
    }

}
