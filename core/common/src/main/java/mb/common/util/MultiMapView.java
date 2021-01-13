package mb.common.util;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A wrapper around a {@link MultiMap multi-map} with read-only operations. Only {@link Serializable serializable} when
 * the wrapped map is.
 *
 * @param <K> The type of keys in this collection.
 * @param <V> The type of values in this collection.
 */
@SuppressWarnings("unused")
public class MultiMapView<K, V> implements Iterable<Map.Entry<K, ArrayList<V>>>, Serializable {
    private final MultiMap<K, V> map;

    private transient @MonotonicNonNull @Nullable SetView<K> keySet = null;
    private transient @MonotonicNonNull @Nullable CollectionView<ArrayList<V>> values = null;
    private transient @MonotonicNonNull @Nullable SetView<Map.Entry<K, ArrayList<V>>> entrySet = null;


    public MultiMapView(MultiMap<K, V> map) {
        this.map = map;
    }

    public static <K, V> MultiMapView<K, V> of() {
        return new MultiMapView<>(MultiMap.withHash());
    }

    public static <K, V> MultiMapView<K, V> of(K key, V value) {
        final MultiMap<K, V> map = MultiMap.withHash();
        map.put(key, value);
        return new MultiMapView<>(map);
    }

    public static <K, V> MultiMapView<K, V> of(K key1, V value1, K key2, V value2) {
        final MultiMap<K, V> map = MultiMap.withHash();
        map.put(key1, value1);
        map.put(key2, value2);
        return new MultiMapView<>(map);
    }

    public static <K, V> MultiMapView<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3) {
        final MultiMap<K, V> map = MultiMap.withHash();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return new MultiMapView<>(map);
    }

    @SafeVarargs public static <K, V> MultiMapView<K, V> of(EntryView<? extends K, ? extends V>... entries) {
        final MultiMap<K, V> map = MultiMap.withHash();
        for(EntryView<? extends K, ? extends V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return new MultiMapView<>(map);
    }

    public static <K, V> MultiMapView<K, V> of(MultiMap<K, V> map) {
        return new MultiMapView<>(map);
    }

    public static <K, V> MultiMapView<K, V> of(Supplier<MultiMap<K, V>> mapSupplier) {
        return new MultiMapView<>(mapSupplier.get());
    }


    public static <K, V> MultiMapView<K, V> copyOf(MultiMap<K, V> map) {
        return new MultiMapView<>(new MultiMap<>(map));
    }

    public static <K, V> MultiMapView<K, V> copyOf(MultiMapView<K, V> map) {
        return new MultiMapView<>(new MultiMap<>(map.map));
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

    public ArrayList<V> get(K key) {
        return map.get(key);
    }

    public SetView<K> keySet() {
        if(keySet == null) {
            keySet = new SetView<>(map.keySet());
        }
        return keySet;
    }

    public CollectionView<ArrayList<V>> values() {
        if(values == null) {
            values = new CollectionView<>(map.values());
        }
        return values;
    }

    public SetView<Map.Entry<K, ArrayList<V>>> entrySet() {
        // TODO: should return EntryViews.
        if(entrySet == null) {
            entrySet = new SetView<>(map.entrySet());
        }
        return entrySet;
    }

//    public V getOrDefault(K key, V defaultValue) {
//        return map.getOrDefault(key, defaultValue);
//    }

    public void forEachValue(BiConsumer<? super K, ? super V> action) {
        map.forEachValue(action);
    }

    public Stream<Map.Entry<K, ArrayList<V>>> stream() {
        // TODO: should return EntryViews.
        return StreamSupport.stream(spliterator(), false);
    }

    public Stream<Map.Entry<K, ArrayList<V>>> parallelStream() {
        // TODO: should return EntryViews.
        return StreamSupport.stream(spliterator(), true);
    }

    @Override public Iterator<Map.Entry<K, ArrayList<V>>> iterator() {
        // TODO: should return iterators over EntryViews.
        return new Iterator<Map.Entry<K, ArrayList<V>>>() {
            private final Iterator<Map.Entry<K, ArrayList<V>>> i = map.entrySet().iterator();

            public boolean hasNext() {
                return i.hasNext();
            }

            public Map.Entry<K, ArrayList<V>> next() {
                return i.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    public void addAllTo(MultiMap<K, V> map) {
        map.putAll(this.map);
    }

    public Map<K, ArrayList<V>> asUnmodifiable() {
        return Collections.unmodifiableMap(map.getInnerMap());
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final MultiMapView<?, ?> other = (MultiMapView<?, ?>)obj;
        return map.equals(other.map);
    }

    @Override public int hashCode() {
        return Objects.hash(map);
    }

    @Override public String toString() {
        return map.toString();
    }
}
