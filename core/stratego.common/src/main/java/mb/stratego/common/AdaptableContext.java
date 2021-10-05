package mb.stratego.common;

import mb.common.util.ClassToInstanceMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AdaptableContext {
    private final ClassToInstanceMap<Object> map;

    public AdaptableContext() {
        this.map = new ClassToInstanceMap<>();
    }

    public AdaptableContext(AdaptableContext adaptableContext) {
        this.map = new ClassToInstanceMap<>(adaptableContext.map);
    }

    public <T> @Nullable T get(Class<T> type) {
        return map.get(type);
    }

    public void put(Object value) {
        map.put(value);
    }

    public <T> void put(Class<T> clazz, T value) {
        map.put(clazz, value);
    }

    public void putAll(AdaptableContext context) {
        map.putAll(context.map);
    }

    /**
     * Attempts to adapt given {@code obj} to given {@code type}, or throws a runtime exception.
     *
     * @param obj        Context object to adapt.
     * @param targetType Reified type to adapt to.
     * @param <T>        Type to adapt to.
     * @return Object of given type.
     * @throws AdaptException when {@code obj} is {@code null}.
     * @throws AdaptException when {@code obj} is not of {@code type} nor a {@link AdaptableContext}.
     * @throws AdaptException when {@code obj} is a {@link AdaptableContext} which does not contain {@code type}.
     */
    public static <T> T adaptContextObject(@Nullable Object obj, Class<T> targetType) {
        if(obj == null) {
            throw new AdaptException("Cannot adapt to type '" + targetType + "', context object is null");
        }
        final Class<?> type = obj.getClass();
        if(type.equals(targetType)) {
            @SuppressWarnings("unchecked") final T value = (T)obj;
            return value;
        }
        if(type.equals(AdaptableContext.class)) {
            final AdaptableContext adaptableContext = (AdaptableContext)obj;
            final @Nullable T value = adaptableContext.get(targetType);
            if(value == null) {
                throw new AdaptException("Cannot adapt to type '" + targetType + "', context object is an AdaptableContext, but does not contain that type");
            }
            return value;
        }
        throw new AdaptException("Cannot adapt to type '" + targetType + "', context object is of unrelated type '" + obj.getClass() + "'");
    }
}
