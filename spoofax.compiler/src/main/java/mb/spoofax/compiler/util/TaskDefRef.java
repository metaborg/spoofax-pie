package mb.spoofax.compiler.util;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface TaskDefRef extends Serializable {
    class Builder extends ImmutableTaskDefRef.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableTaskDefRef of(String name, ClassInfo type) {
        return ImmutableTaskDefRef.of(name, type);
    }

    static ImmutableTaskDefRef of(ClassInfo type, UniqueNamer uniqueNamer) {
        return ImmutableTaskDefRef.of(uniqueNamer.makeUnique(type.asVariableId()), type);
    }


    @Value.Parameter String name();

    @Value.Parameter ClassInfo type();
}
