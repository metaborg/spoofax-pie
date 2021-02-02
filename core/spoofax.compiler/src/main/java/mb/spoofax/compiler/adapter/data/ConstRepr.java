package mb.spoofax.compiler.adapter.data;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface ConstRepr extends ArgRepr {
    class Builder extends ImmutableConstRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableConstRepr ofNull() {
        return ImmutableConstRepr.of("null");
    }

    static ImmutableConstRepr of(String value) {
        return ImmutableConstRepr.of(value);
    }

    static ImmutableConstRepr of(boolean value) {
        return ImmutableConstRepr.of(value ? "true" : "false");
    }

    @Value.Parameter String value();

    @Override
    default String toJava() { return value(); }

}
