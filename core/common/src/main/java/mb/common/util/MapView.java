package mb.common.util;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A wrapper around a {@link Map map} with read-only operations. Only {@link Serializable serializable} when the wrapped
 * map is.
 *
 * @param <K> The type of keys in this collection.
 * @param <V> The type of values in this collection.
 */
@SuppressWarnings("unused")
public class MapView<K, V> implements Iterable<Map.Entry<K, V>>, Serializable {
    private final Map<K, V> map;

    private transient @MonotonicNonNull @Nullable SetView<K> keySet = null;
    private transient @MonotonicNonNull @Nullable CollectionView<V> values = null;
    private transient @MonotonicNonNull @Nullable SetView<Map.Entry<K, V>> entrySet = null;


    public MapView(Map<K, V> map) {
        this.map = map;
    }

    public static <K, V> MapView<K, V> of() {
        return new MapView<>(Collections.emptyMap());
    }

    public static <K, V> MapView<K, V> of(K key, V value) {
        final HashMap<K, V> map = new HashMap<>();
        map.put(key, value);
        return new MapView<>(map);
    }

    public static <K, V> MapView<K, V> of(K key1, V value1, K key2, V value2) {
        final HashMap<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return new MapView<>(map);
    }

    public static <K, V> MapView<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3) {
        final HashMap<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return new MapView<>(map);
    }

    @SafeVarargs public static <K, V> MapView<K, V> of(EntryView<? extends K, ? extends V>... entries) {
        final HashMap<K, V> map = new HashMap<>();
        for(EntryView<? extends K, ? extends V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return new MapView<>(map);
    }

    public static <K, V> MapView<K, V> of(Map<K, V> map) {
        return new MapView<>(map);
    }

    public static <K, V> MapView<K, V> of(Supplier<Map<K, V>> mapSupplier) {
        return new MapView<>(mapSupplier.get());
    }


    public static <K, V> MapView<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return new MapView<>(new HashMap<>(map));
    }

    public static <K, V> MapView<K, V> copyOf(MapView<? extends K, ? extends V> map) {
        return new MapView<>(new HashMap<>(map.map));
    }


    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public boolean containsValue(V value) {
        return map.containsValue(value);
    }

    public @Nullable V get(K key) {
        return map.get(key);
    }

    public SetView<K> keySet() {
        if(keySet == null) {
            keySet = new SetView<>(map.keySet());
        }
        return keySet;
    }

    public CollectionView<V> values() {
        if(values == null) {
            values = new CollectionView<>(map.values());
        }
        return values;
    }

    public SetView<Map.Entry<K, V>> entrySet() {
        // TODO: should return EntryViews.
        if(entrySet == null) {
            entrySet = new SetView<>(map.entrySet());
        }
        return entrySet;
    }

    public V getOrDefault(K key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    public Stream<Map.Entry<K, V>> stream() {
        // TODO: should return EntryViews.
        return StreamSupport.stream(spliterator(), false);
    }

    public Stream<Map.Entry<K, V>> parallelStream() {
        // TODO: should return EntryViews.
        return StreamSupport.stream(spliterator(), true);
    }

    @Override public Iterator<Map.Entry<K, V>> iterator() {
        // TODO: should return iterators over EntryViews.
        return new Iterator<Map.Entry<K, V>>() {
            private final Iterator<Map.Entry<K, V>> i = map.entrySet().iterator();

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


    public void addAllTo(Map<K, V> map) {
        map.putAll(this.map);
    }

    public Map<K, V> asUnmodifiable() {
        return Collections.unmodifiableMap(map);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final MapView<?, ?> other = (MapView<?, ?>)obj;
        return map.equals(other.map);
    }

    @Override public int hashCode() {
        return Objects.hash(map);
    }

    @Override public String toString() {
        return map.toString();
    }
}
