package mb.stratego.common;

import mb.common.util.ClassToInstanceMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompositeContextObject {
    private final ClassToInstanceMap<Object> map = new ClassToInstanceMap<>();

    public <T> @Nullable T get(Class<T> type) {
        return map.get(type);
    }

    public <T> void put(Class<T> type, T value) {
        map.put(type, value);
    }


    public static <T> T adaptContextObject(@Nullable Object contextObject, Class<T> type) {
        if(contextObject == null) {
            throw new RuntimeException("Cannot adapt to type '" + type + "', context object is empty");
        }
        if(type.equals(contextObject.getClass())) {
            @SuppressWarnings("unchecked") final T value = (T)contextObject;
            return value;
        }
        if(type.equals(CompositeContextObject.class)) {
            final CompositeContextObject composite = (CompositeContextObject)contextObject;
            final @Nullable T value = composite.get(type);
            throw new RuntimeException("Cannot adapt to type '" + type + "', context object is a CompositeContextObject, but does not contain that type");
        }
        throw new RuntimeException("Cannot adapt to type '" + type + "', context object is of unrelated type '" + contextObject.getClass() + "'");
    }
}
