package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;

public class ClassToInstanceMap<C> implements Serializable {
    private final HashMap<Class<? extends C>, C> map = new HashMap<>();


    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsType(Class<? extends C> type) {
        return map.containsKey(type);
    }

    public <T extends C> @Nullable T get(Class<T> type) {
        final @Nullable C instance = map.get(type);
        if(instance == null || !instance.getClass().equals(type)) {
            return null;
        }
        @SuppressWarnings("unchecked") final T value = (T)map.get(type);
        return value;
    }

    public <T extends C> void put(Class<T> type, T value) {
        map.put(type, value);
    }
}
