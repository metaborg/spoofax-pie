package mb.spoofax.compiler.util;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface NamedInjection extends Serializable {
    class Builder extends ImmutableNamedInjection.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableNamedInjection of(TypeInfo type, UniqueNamer uniqueNamer) {
        return ImmutableNamedInjection.of(uniqueNamer.makeUnique(type.asVariableId()), type);
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
