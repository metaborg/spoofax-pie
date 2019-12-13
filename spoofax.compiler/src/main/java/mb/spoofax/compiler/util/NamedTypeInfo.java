package mb.spoofax.compiler.util;

import org.immutables.value.Value;

@Value.Immutable
public interface NamedTypeInfo {
    class Builder extends ImmutableNamedTypeInfo.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableNamedTypeInfo of(String name, TypeInfo type) {
        return ImmutableNamedTypeInfo.of(name, type);
    }


    @Value.Parameter String name();

    @Value.Parameter TypeInfo type();

    default String variable() {
        return type().qualifiedId() + " " + name();
    }

    default String thisAssign() {
        return "this." + name() + " = " + name();
    }
}
