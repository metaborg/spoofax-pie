package mb.spoofax.compiler.util;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface Named<T> extends Serializable {
    class Builder<T> extends ImmutableNamed.Builder<T> {}

    static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    static <T> ImmutableNamed<T> of(T value, String name, UniqueNamer uniqueNamer) {
        return ImmutableNamed.of(uniqueNamer.makeUnique(name), value);
    }


    @Value.Parameter String name();

    @Value.Parameter T value();
}
