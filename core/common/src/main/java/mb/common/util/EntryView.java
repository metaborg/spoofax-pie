package mb.common.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class EntryView<K, V> implements Serializable {
    private final K key;
    private final V value;


    public EntryView(Map.Entry<K, V> entry) {
        this.key = entry.getKey();
        this.value = entry.getValue();
    }

    public EntryView(K key, V value) {
        this.key = key;
        this.value = value;
    }


    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final EntryView<?, ?> entryView = (EntryView<?, ?>)o;
        return Objects.equals(key, entryView.key) &&
            Objects.equals(value, entryView.value);
    }

    @Override public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override public String toString() {
        return key.toString() + " : " + value.toString();
    }
}
