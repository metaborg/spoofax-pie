package mb.spoofax.compiler.util;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface Named<T> extends Serializable {
    class Builder<T> extends ImmutableNamed.Builder<T> {}

    static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    static <T> ImmutableNamed<T> of(String name, T value) {
        return ImmutableNamed.of(name, value);
    }


    @Value.Parameter String name();

    @Value.Parameter T value();
}
