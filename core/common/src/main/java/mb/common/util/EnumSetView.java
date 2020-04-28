package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * A wrapper around an {@link EnumSet enum set} with read-only operations. Only {@link Serializable serializable} when
 * the wrapped set is.
 *
 * @param <E> The type of elements in this collection.
 */
@SuppressWarnings("unused")
public class EnumSetView<E extends Enum<E>> extends BaseCollectionView<E, EnumSet<? extends E>> implements Iterable<E>, Serializable {
    public EnumSetView(EnumSet<? extends E> collection) {
        super(collection);
    }

    public static <E extends Enum<E>> EnumSetView<E> noneOf(Class<E> elementType) {
        return new EnumSetView<>(EnumSet.noneOf(elementType));
    }

    public static <E extends Enum<E>> EnumSetView<E> of(E element) {
        return new EnumSetView<>(EnumSet.of(element));
    }

    @SafeVarargs public static <E extends Enum<E>> EnumSetView<E> of(E element, E... elements) {
        return new EnumSetView<>(EnumSet.of(element, elements));
    }

    public static <E extends Enum<E>> EnumSetView<E> of(EnumSet<? extends E> enumSet) {
        return new EnumSetView<>(enumSet);
    }


    public static <E extends Enum<E>> EnumSetView<E> copyOf(EnumSet<E> enumSet) {
        return new EnumSetView<>(EnumSet.copyOf(enumSet));
    }


    @Override public Set<E> asUnmodifiable() {
        return Collections.unmodifiableSet(collection);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final EnumSetView<?> other = (EnumSetView<?>)obj;
        return collection.equals(other.collection);
    }

    @Override public int hashCode() {
        return Objects.hash(collection);
    }

    @Override public String toString() {
        return collection.toString();
    }
}
