package mb.spoofax.compiler.adapter.data;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface VarRepr extends ArgRepr {
    class Builder extends ImmutableVarRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableVarRepr of(String id) {
        return ImmutableVarRepr.of(id);
    }

    @Value.Parameter String id();

    @Override
    default String toJava() { return id(); }
}
