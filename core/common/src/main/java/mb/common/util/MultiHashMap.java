package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class MultiHashMap<K, V> implements Serializable {
    private final HashMap<K, ArrayList<V>> map;


    public MultiHashMap() {
        this.map = new HashMap<>();
    }

    public MultiHashMap(MultiHashMap<K, V> map) {
        this.map = new HashMap<>(map.map);
    }

    public MultiHashMap(Map<? extends K, ArrayList<V>> map) {
        this.map = new HashMap<>(map);
    }


    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public ArrayList<V> get(K key) {
        return map.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public void put(@Nullable K key, V value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void putAll(@Nullable K key, Collection<? extends V> values) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
    }

    public void putAll(@Nullable K key, Iterable<? extends V> values) {
        final ArrayList<V> list = map.computeIfAbsent(key, k -> new ArrayList<>());
        IterableUtil.addAll(list, values);
    }

    public void putAll(Map<? extends K, ? extends Collection<? extends V>> mapping) {
        for(Entry<? extends K, ? extends Collection<? extends V>> entry : mapping.entrySet()) {
            final K key = entry.getKey();
            final Collection<? extends V> values = entry.getValue();
            putAll(key, values);
        }
    }

    public void putAll(MultiHashMap<K, V> mapping) {
        putAll(mapping.map);
    }

    public void replaceAll(@Nullable K key, ArrayList<V> values) {
        map.put(key, values);
    }

    public void replaceAll(Map<? extends K, ArrayList<V>> mapping) {
        for(Entry<? extends K, ArrayList<V>> entry : mapping.entrySet()) {
            final K key = entry.getKey();
            final ArrayList<V> values = entry.getValue();
            replaceAll(key, values);
        }
    }

    public void replaceAll(MultiHashMap<K, V> mapping) {
        replaceAll(mapping.map);
    }

    public void removeAll(K key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<ArrayList<V>> values() {
        return map.values();
    }

    public Set<Entry<K, ArrayList<V>>> entrySet() {
        return map.entrySet();
    }


    public HashMap<K, ArrayList<V>> getInnerMap() {
        return map;
    }


    public void forEach(BiConsumer<? super K, ArrayList<? super V>> action) {
        for(Entry<K, ArrayList<V>> entry : map.entrySet()) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

    public void forEachValue(BiConsumer<? super K, ? super V> action) {
        for(Entry<K, ArrayList<V>> entry : map.entrySet()) {
            final K key = entry.getKey();
            final ArrayList<V> values = entry.getValue();
            for(V value : values) {
                action.accept(key, value);
            }
        }
    }
}
