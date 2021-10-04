package mb.tego.tuples;

import java.util.Iterator;

/**
 * A tuple with a number of components.
 */
public interface Tuple {

    /**
     * Creates a tuple from the specified iterable.
     *
     * @param components the components in the tuple
     * @return the tuple
     */
    static Tuple from(Iterable<?> components) {
        final Iterator<?> iterator = components.iterator();
        if (!iterator.hasNext()) throw new IllegalArgumentException("A tuple with 0 elements (Unit) is not supported. Use java.lang.Void instead.");
        final Object c1 = iterator.next();
        if (!iterator.hasNext()) throw new IllegalArgumentException("A tuple with 1 element (Singleton) is not supported. Use the element itself instead.");
        final Object c2 = iterator.next();
        if (!iterator.hasNext()) return new Pair<>(c1, c2);

        throw new IllegalArgumentException("A tuple with more than 2 elements is not currently supported.");
    }

    /**
     * The number of components in the tuple.
     *
     * @return the number of components
     */
    int getArity();

    /**
     * Gets the component with the specified index.
     *
     * @param index the zero-based index of the component
     * @return the component
     * @throws IndexOutOfBoundsException if the index is not between 0 and {@link #getArity} (exclusive).
     */
    Object get(int index);

}
