package mb.common.util;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A wrapper around a {@link Map map} with read-only operations. Only {@link Serializable serializable} when the wrapped
 * map is.
 *
 * @param <K> The type of keys in this collection.
 * @param <V> The type of values in this collection.
 */
public class MapView<K, V> implements Iterable<Map.Entry<K, V>>, Serializable {
    private final Map<K, V> collection;

    private transient @MonotonicNonNull @Nullable SetView<K> keySet = null;
    private transient @MonotonicNonNull @Nullable SetView<Map.Entry<K, V>> entrySet = null;
    private transient @MonotonicNonNull @Nullable CollectionView<V> values = null;


    public MapView(Map<K, V> map) {
        this.collection = map;
    }

    public static <K, V> MapView<K, V> of() {
        return new MapView<>(Collections.emptyMap());
    }

    public static <K, V> MapView<K, V> of(K key, V value) {
        final HashMap<K, V> map = new HashMap<>();
        map.put(key, value);
        return new MapView<>(map);
    }

    @SafeVarargs public static <K, V> MapView<K, V> of(Map.Entry<K, V>... entries) {
        final HashMap<K, V> map = new HashMap<>();
        for(Map.Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return new MapView<>(map);
    }


    public int size() {
        return collection.size();
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }

    public boolean containsKey(K key) {
        return collection.containsKey(key);
    }

    public boolean containsValue(V value) {
        return collection.containsValue(value);
    }

    public @Nullable V get(K key) {
        return collection.get(key);
    }

    public SetView<K> keySet() {
        if(keySet == null) {
            keySet = new SetView<>(collection.keySet());
        }
        return keySet;
    }

    // TODO: should return EntryView's
    public SetView<Map.Entry<K, V>> entrySet() {
        if(entrySet == null) {
            entrySet = new SetView<>(collection.entrySet());
        }
        return entrySet;
    }

    public CollectionView<V> values() {
        if(values == null) {
            values = new CollectionView<>(collection.values());
        }
        return values;
    }

    public V getOrDefault(K key, V defaultValue) {
        return collection.getOrDefault(key, defaultValue);
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        collection.forEach(action);
    }

    // TODO: should return EntryView's
    public Stream<Map.Entry<K, V>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    // TODO: should return EntryView's
    public Stream<Map.Entry<K, V>> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    // TODO: should return EntryView's
    @Override public Iterator<Map.Entry<K, V>> iterator() {
        return new Iterator<Map.Entry<K, V>>() {
            private final Iterator<? extends Map.Entry<K, V>> i = collection.entrySet().iterator();


            public boolean hasNext() {
                return i.hasNext();
            }

            public Map.Entry<K, V> next() {
                return i.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    public Map<K, V> asUnmodifiable() {
        return Collections.unmodifiableMap(collection);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final MapView<?, ?> other = (MapView<?, ?>) obj;
        return collection.equals(other.collection);
    }

    @Override public int hashCode() {
        return Objects.hash(collection);
    }

    @Override public String toString() {
        return collection.toString();
    }
}
