package mb.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class ClassToInstanceMap<C> implements Serializable {
    private final HashMap<Class<?>, C> map;

    public ClassToInstanceMap() {
        this.map = new HashMap<>();
    }

    public <T extends C> ClassToInstanceMap(T value) {
        this.map = new HashMap<>();
        this.put(value);
    }

    public <T extends C> ClassToInstanceMap(Iterable<T> values) {
        this.map = new HashMap<>();
        for(T value : values) {
            this.put(value);
        }
    }

    @SafeVarargs public <T extends C> ClassToInstanceMap(T... values) {
        this.map = new HashMap<>();
        for(T value : values) {
            this.put(value);
        }
    }

    public <T extends C> ClassToInstanceMap(ClassToInstanceMap<T> map) {
        this.map = new HashMap<>(map.map);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsType(Class<? extends C> type) {
        return map.containsKey(type);
    }

    /**
     * Gets the instance for given {@code type}, with the type of the returned instance guaranteed to be of type {@code
     * type}. Returns {@code null} if there is no instance for given type, or when the type of retrieved instance does
     * not match {@code type}.
     *
     * @param type Reified type of the instance to get.
     * @param <T>  Type of the instance to get.
     * @return Instance for given type, {@code null} otherwise.
     */
    public <T extends C> @Nullable T get(Class<T> type) {
        final @Nullable C instance = map.get(type);
        if(instance == null || !instance.getClass().equals(type)) {
            return null;
        }
        @SuppressWarnings("unchecked") final T value = (T)map.get(type);
        return value;
    }

    public Collection<C> values() {
        return map.values();
    }

    public <T extends C> void put(T value) {
        map.put(value.getClass(), value);
    }

    public <T extends C> void putAll(ClassToInstanceMap<T> map) {
        this.map.putAll(map.map);
    }
}
