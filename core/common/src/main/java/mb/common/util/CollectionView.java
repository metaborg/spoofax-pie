package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * A wrapper around a {@link Collection collection} with read-only operations. Only {@link Serializable serializable}
 * when the wrapped collection is.
 *
 * @param <E> The type of elements in this collection.
 */
@SuppressWarnings("unused")
public class CollectionView<E> extends BaseCollectionView<E, Collection<? extends E>> {
    public CollectionView(Collection<? extends E> collection) {
        super(collection);
    }

    public static <E> CollectionView<E> of() {
        return new CollectionView<>(Collections.emptyList());
    }

    public static <E> CollectionView<E> of(E element) {
        final ArrayList<E> collection = new ArrayList<>();
        collection.add(element);
        return new CollectionView<>(collection);
    }

    @SafeVarargs public static <E> CollectionView<E> of(E... elements) {
        final ArrayList<E> collection = new ArrayList<>();
        Collections.addAll(collection, elements);
        return new CollectionView<>(collection);
    }

    public static <E> CollectionView<E> of(Collection<? extends E> collection) {
        return new CollectionView<>(collection);
    }


    public static <E> CollectionView<E> copyOf(Iterable<? extends E> elements) {
        final ArrayList<E> list = new ArrayList<>();
        IterableUtil.addAll(list, elements);
        return new CollectionView<>(list);
    }

    public static <E> CollectionView<E> copyOf(Collection<? extends E> collection) {
        return new CollectionView<>(new ArrayList<>(collection));
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final CollectionView<?> other = (CollectionView<?>)obj;
        return collection.equals(other.collection);
    }

    @Override public int hashCode() {
        return Objects.hash(collection);
    }

    @Override public String toString() {
        return collection.toString();
    }
}
