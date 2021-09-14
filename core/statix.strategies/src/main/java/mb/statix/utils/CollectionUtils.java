package mb.statix.utils;

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
}
