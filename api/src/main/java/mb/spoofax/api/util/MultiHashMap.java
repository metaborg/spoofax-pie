package mb.spoofax.api.util;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public class MultiHashMap<K, V> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final HashMap<K, ArrayList<V>> map = new HashMap<>();


    public ArrayList<V> get(K key) {
        return map.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<ArrayList<V>> values() {
        return map.values();
    }

    public Set<Map.Entry<K, ArrayList<V>>> entrySet() {
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
