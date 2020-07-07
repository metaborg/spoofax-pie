package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A wrapper around a {@link List list} with read-only operations. Only {@link Serializable serializable} when the
 * wrapped list is.
 *
 * @param <E> The type of elements in this list.
 */
@SuppressWarnings("unused")
public class ListView<E> extends BaseCollectionView<E, List<? extends E>> implements Iterable<E>, Serializable {
    public ListView(List<? extends E> collection) {
        super(collection);
    }

    public static <E> ListView<E> of() {
        return new ListView<>(Collections.emptyList());
    }

    public static <E> ListView<E> of(E element) {
        final ArrayList<E> list = new ArrayList<>();
        list.add(element);
        return new ListView<>(list);
    }

    @SafeVarargs public static <E> ListView<E> of(E... elements) {
        final ArrayList<E> list = new ArrayList<>();
        Collections.addAll(list, elements);
        return new ListView<>(list);
    }

    public static <E> ListView<E> of(List<? extends E> list) {
        return new ListView<>(list);
    }


    public static <E> ListView<E> copyOf(Iterable<? extends E> iterable) {
        final ArrayList<E> list = new ArrayList<>();
        IterableUtil.addAll(list, iterable);
        return new ListView<>(list);
    }

    public static <E> ListView<E> copyOf(Collection<? extends E> collection) {
        return new ListView<>(new ArrayList<>(collection));
    }

    public static <E> ListView<E> copyOf(ListView<? extends E> list) {
        return new ListView<>(new ArrayList<>(list.collection));
    }


    public E get(int index) {
        return collection.get(index);
    }

    public int indexOf(E element) {
        return collection.indexOf(element);
    }

    public int lastIndexOf(E element) {
        return collection.lastIndexOf(element);
    }

    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    public ListIterator<E> listIterator(final int index) {
        return new ListIterator<E>() {
            private final ListIterator<? extends E> i = collection.listIterator(index);


            @Override public boolean hasNext() {
                return i.hasNext();
            }

            @Override public E next() {
                return i.next();
            }

            @Override public boolean hasPrevious() {
                return i.hasPrevious();
            }

            @Override public E previous() {
                return i.previous();
            }

            @Override public int nextIndex() {
                return i.nextIndex();
            }

            @Override public int previousIndex() {
                return i.previousIndex();
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override public void set(E e) {
                throw new UnsupportedOperationException();
            }

            @Override public void add(E e) {
                throw new UnsupportedOperationException();
            }

            @Override public void forEachRemaining(Consumer<? super E> action) {
                i.forEachRemaining(action);
            }
        };
    }

    ListView<E> subList(int fromIndex, int toIndex) {
        return new ListView<>(collection.subList(fromIndex, toIndex));
    }


    public void addAllTo(List<E> list) {
        list.addAll(this.collection);
    }

    @Override public List<E> asUnmodifiable() {
        return Collections.unmodifiableList(collection);
    }

    @Override public ArrayList<E> asCopy() {
        return new ArrayList<>(collection);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final ListView<?> other = (ListView<?>)obj;
        return collection.equals(other.collection);
    }

    @Override public int hashCode() {
        return Objects.hash(collection);
    }

    @Override public String toString() {
        return collection.toString();
    }
}
