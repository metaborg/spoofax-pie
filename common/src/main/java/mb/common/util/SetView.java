package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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


    @Override public Set<E> asUnmodifiable() {
        return Collections.unmodifiableSet(collection);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final SetView<?> other = (SetView<?>) obj;
        return collection.equals(other.collection);
    }

    @Override public int hashCode() {
        return Objects.hash(collection);
    }

    @Override public String toString() {
        return collection.toString();
    }
}
