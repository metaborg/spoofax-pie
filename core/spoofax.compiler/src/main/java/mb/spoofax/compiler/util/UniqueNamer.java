package mb.spoofax.compiler.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class UniqueNamer {
    private final Optional<TypeInfo> scope;
    private final Optional<TypeInfo> qualifier;
    private final HashMap<String, AtomicInteger> nameToNextInteger = new HashMap<>();
    private final HashMap<Object, String> names = new HashMap<>();

    public UniqueNamer(TypeInfo scope, TypeInfo qualifier) {
        this.scope = Optional.of(scope);
        this.qualifier = Optional.of(qualifier);
    }

    public UniqueNamer() {
        this.scope = Optional.empty();
        this.qualifier = Optional.empty();
    }

    public void reserve(String name) {
        if(!nameToNextInteger.containsKey(name)) {
            nameToNextInteger.put(name, new AtomicInteger(0));
        }
    }

    public NamedTypeInfo makeUnique(TypeInfo type) {
        final String newName = makeUnique(type.asVariableId());
        names.put(type, newName);
        return NamedTypeInfo.of(newName, type, scope, qualifier);
    }

    public <T> Named<T> makeUnique(T value, String name) {
        final String newName = makeUnique(name);
        names.put(value, newName);
        return Named.of(newName, value);
    }

    public <T> Named<T> makeUnique(T value, Function<T, String> getName) {
        final String newName = makeUnique(getName.apply(value));
        names.put(value, newName);
        return Named.of(newName, value);
    }

    public @Nullable String getNameFor(Object value) {
        return names.get(value);
    }

    public void reset() {
        nameToNextInteger.clear();
        names.clear();
    }


    private String makeUnique(String name) {
        while(true) {
            final @Nullable AtomicInteger counter = nameToNextInteger.get(name);
            if(counter == null) {
                nameToNextInteger.put(name, new AtomicInteger(0));
                return name;
            }
            final int count = counter.incrementAndGet();
            if(count == 0) {
                throw new IllegalStateException("Cannot create unique name from '" + name + "' counter wrapped around");
            }
            final String newName = name + count;
            if(!nameToNextInteger.containsKey(newName)) {
                return newName;
            }
        }
    }
}
