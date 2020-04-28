package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A wrapper around a {@link Set set} with read-only operations. Only {@link Serializable serializable} when the wrapped
 * set is.
 *
 * @param <E> The type of elements in this collection.
 */
@SuppressWarnings("unused")
public class SetView<E> extends BaseCollectionView<E, Set<? extends E>> implements Iterable<E>, Serializable {
    public SetView(Set<? extends E> collection) {
        super(collection);
    }

    public static <E> SetView<E> of() {
        return new SetView<>(Collections.emptySet());
    }

    public static <E> SetView<E> of(E element) {
        final HashSet<E> set = new HashSet<>();
        set.add(element);
        return new SetView<>(set);
    }

    @SafeVarargs public static <E> SetView<E> of(E... elements) {
        final HashSet<E> set = new HashSet<>();
        Collections.addAll(set, elements);
        return new SetView<>(set);
    }

    public static <E> SetView<E> of(Set<? extends E> set) {
        return new SetView<>(set);
    }


    public static <E> SetView<E> copyOf(Iterable<? extends E> elements) {
        final HashSet<E> set = new HashSet<>();
        IterableUtil.addAll(set, elements);
        return new SetView<>(set);
    }

    public static <E> SetView<E> copyOf(Set<? extends E> set) {
        return new SetView<>(new HashSet<>(set));
    }


    public void addAllTo(Set<E> set) {
        set.addAll(this.collection);
    }

    @Override public Set<E> asUnmodifiable() {
        return Collections.unmodifiableSet(collection);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final SetView<?> other = (SetView<?>)obj;
        return collection.equals(other.collection);
    }

    @Override public int hashCode() {
        return Objects.hash(collection);
    }

    @Override public String toString() {
        return collection.toString();
    }
}
