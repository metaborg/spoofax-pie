package mb.common.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

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


    public ArrayList<V> get(K key) {
        return map.computeIfAbsent(key, k -> new ArrayList<>());
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

    public HashMap<K, ArrayList<V>> getAll() {
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


    public void add(K key, V value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void addAll(K key, Collection<? extends V> values) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
    }

    public void addAll(Map<? extends K, ? extends Collection<? extends V>> mapping) {
        for(Entry<? extends K, ? extends Collection<? extends V>> entry : mapping.entrySet()) {
            final K key = entry.getKey();
            final Collection<? extends V> values = entry.getValue();
            addAll(key, values);
        }
    }

    public void addAll(MultiHashMap<K, V> mapping) {
        addAll(mapping.map);
    }

    public void replaceAll(K key, ArrayList<V> values) {
        map.put(key, values);
    }

    public void removeAll(K key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }
}
