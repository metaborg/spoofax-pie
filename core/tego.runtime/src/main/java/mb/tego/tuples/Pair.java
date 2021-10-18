package mb.tego.tuples;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable tuple with two components.
 *
 * @param <T1> the type of the first component (contravariant)
 * @param <T2> the type of the second component (contravariant)
 */
public final class Pair<T1, T2> implements Tuple, Map.Entry<T1, T2>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link Pair} from the specified components.
     *
     * @param component1 the first component
     * @param component2 the second component
     * @param <T1> the type of the first component (contravariant)
     * @param <T2> the type of the second component (contravariant)
     * @return the resulting tuple
     */
    public static <T1, T2> Pair<T1, T2> of(T1 component1, T2 component2) {
        return new Pair<>(component1, component2);
    }

    /**
     * Creates a tuple from the specified iterable.
     *
     * This shadows the {@link Tuple#from} method.
     *
     * @param components the components in the tuple
     * @return the tuple
     */
    public static <T> Pair<T, T> from(Iterable<T> components) {
        final Iterator<T> iterator = components.iterator();
        if (!iterator.hasNext()) throw new IllegalArgumentException("Expected iterable with 2 elements, found 0.");
        final T c1 = iterator.next();
        if (!iterator.hasNext()) throw new IllegalArgumentException("Expected iterable with 2 elements, found 1.");
        final T c2 = iterator.next();
        if (iterator.hasNext()) throw new IllegalArgumentException("Expected iterable with 2 elements, found more.");

        return new Pair<>(c1, c2);
    }

    /**
     * Creates a tuple from the specified entry.
     *
     * @param entry the entry
     * @param <T1> the type of the first component (contravariant)
     * @param <T2> the type of the second component (contravariant)
     * @return the resulting tuple
     */
    public static <T1, T2> Pair<T1, T2> from(Map.Entry<T1, T2> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    private final T1 component1;
    private final T2 component2;

    /**
     * Initializes a new instance of the {@link Pair} class.
     *
     * @param component1 the first component in the tuple
     * @param component2 the second component in the tuple
     */
    public Pair(T1 component1, T2 component2) {
        this.component1 = component1;
        this.component2 = component2;
    }

    /**
     * Gets the first component in the tuple.
     *
     * This method is provided for compatibility with Kotlin destructuring declarations.
     *
     * @return the first component in the tuple (which may be {@code null})
     */
    public T1 component1() {
        return component1;
    }

    /**
     * Gets the second component in the tuple.
     *
     * This method is provided for compatibility with Kotlin destructuring declarations.
     *
     * @return the second component in the tuple (which may be {@code null})
     */
    public T2 component2() {
        return component2;
    }

    @Override
    public T1 getKey() {
        return component1;
    }

    @Override
    public T2 getValue() {
        return component2;
    }

    @Override
    @Deprecated
    public T2 setValue(T2 value) {
        throw new UnsupportedOperationException("A tuple is immutable.");
    }

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case 0: return component1;
            case 1: return component2;
            default: throw new IndexOutOfBoundsException("'index' must be between 0 and " + getArity() + " (exclusive).");
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Pair)) return false;
        Pair<?, ?> that = (Pair<?, ?>)o;
        // We use `Objects.equals()` because the items might be `null`
        return Objects.equals(this.component1, that.component1)
            && Objects.equals(this.component2, that.component2);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (component1 != null ? component1.hashCode() : 0);
        hash = 31 * hash + (component2 != null ? component2.hashCode() : 0);
        return hash;
    }

    @Override public String toString() {
        // This will likely be optimized by the compiler into calls to a StringBuilder or similar.
        return "(" + component1 + ", " + component2 + ')';
    }

}
