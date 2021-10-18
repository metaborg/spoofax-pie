package mb.tego.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CollectionUtils {
    private CollectionUtils() { /* Cannot be instantated. */ }

    /**
     * Returns a list for the given iterable.
     *
     * This casts the iterable to a list, if possible.
     * Otherwise, this adds the elements from the iterable
     * to a new list.
     *
     * @param iterable the iterable
     * @param <T> the type of elements
     * @return the resulting list
     */
    public static <T> List<T> listFromIterable(Iterable<T> iterable) {
        final List<T> list;
        if (iterable instanceof List) {
            list = (List<T>)iterable;
        } else {
            if (iterable instanceof Collection) {
                list = new ArrayList<>(((Collection<T>)iterable).size());
            } else {
                list = new ArrayList<>();
            }
            for (T component : iterable) {
                list.add(component);
            }
        }
        return list;
    }

    /**
     * Determines whether the collection contains any of the specified elements.
     *
     * @param collection the collection to check
     * @param elements the elements tp find
     * @param <E> the type of elements
     * @return {@code true} when the collection contains one or more of the given elements;
     * otherwise, {@code false}
     */
    public static <E> boolean containsAny(Collection<E> collection, Collection<E> elements) {
        for (E element : elements) {
            if (collection.contains(element)) return true;
        }
        return false;
    }
}
