package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BaseCollectionView<E, C extends Collection<? extends E>> implements Iterable<E>, Serializable {
    protected final C collection;


    public BaseCollectionView(C collection) {
        this.collection = collection;
    }


    public int size() {
        return collection.size();
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }

    public boolean contains(E element) {
        return collection.contains(element);
    }

    @Override public Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<? extends E> i = collection.iterator();

            @Override public boolean hasNext() {
                return i.hasNext();
            }

            @Override public E next() {
                return i.next();
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override public void forEachRemaining(Consumer<? super E> action) {
                // Use backing collection version
                i.forEachRemaining(action);
            }
        };
    }

    public Object[] toArray() {
        return collection.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return collection.toArray(a);
    }

    public boolean containsAll(Collection<? extends E> other) {
        return collection.containsAll(other);
    }

    @Override public Spliterator<E> spliterator() {
        @SuppressWarnings("unchecked") final Spliterator<E> spliterator = (Spliterator<E>)collection.spliterator();
        return spliterator;
    }

    public Stream<E> stream() {
        @SuppressWarnings("unchecked") final Stream<E> stream = (Stream<E>)collection.stream();
        return stream;
    }

    public Stream<E> parallelStream() {
        @SuppressWarnings("unchecked") final Stream<E> parallelStream = (Stream<E>)collection.parallelStream();
        return parallelStream;
    }


    @Override public void forEach(Consumer<? super E> action) {
        collection.forEach(action);
    }


    public void addAllTo(Collection<E> collection) {
        collection.addAll(this.collection);
    }

    public Collection<E> asUnmodifiable() {
        return Collections.unmodifiableCollection(collection);
    }

    public Collection<E> asCopy() {
        return new ArrayList(collection);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final BaseCollectionView<?, ?> other = (BaseCollectionView<?, ?>)obj;
        return collection.equals(other.collection);
    }

    @Override public int hashCode() {
        return Objects.hash(collection);
    }

    @Override public String toString() {
        return collection.toString();
    }
}
