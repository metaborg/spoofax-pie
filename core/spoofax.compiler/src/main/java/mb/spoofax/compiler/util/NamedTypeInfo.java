package mb.spoofax.compiler.util;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface NamedTypeInfo {
    class Builder extends ImmutableNamedTypeInfo.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableNamedTypeInfo of(String name, TypeInfo type) {
        return ImmutableNamedTypeInfo.of(name, type, Optional.empty(), Optional.empty());
    }

    static ImmutableNamedTypeInfo of(String name, TypeInfo type, Optional<TypeInfo> scope, Optional<TypeInfo> qualifier) {
        return ImmutableNamedTypeInfo.of(name, type, scope, qualifier);
    }

    static ImmutableNamedTypeInfo of(String name, TypeInfo type, TypeInfo scope, TypeInfo qualifier) {
        return ImmutableNamedTypeInfo.of(name, type, Optional.of(scope), Optional.of(qualifier));
    }


    @Value.Parameter String name();

    @Value.Parameter TypeInfo type();

    @Value.Parameter Optional<TypeInfo> scope();

    @Value.Parameter Optional<TypeInfo> qualifier();

    default String variable() {
        return type().qualifiedId() + " " + name();
    }

    default String scopedVariable() {
        return scope().map(scope -> "@" + scope.qualifiedId() + " " + variable()).orElseGet(this::variable);
    }

    default String qualifiedVariable() {
        return qualifier().map(qualifier -> "@" + qualifier.qualifiedId() + " " + variable()).orElseGet(this::variable);
    }

    default String scopedQualifiedVariable() {
        final String scopePart = scope().map(scope -> "@" + scope.qualifiedId() + " ").orElse("");
        final String qualifierPart = qualifier().map(qualifier -> "@" + qualifier.qualifiedId() + " ").orElse("");
        return scopePart + qualifierPart + variable();
    }

    default String thisAssign() {
        return "this." + name() + " = " + name();
    }

    default String getter() {
        return type().qualifiedId() + " get" + StringUtil.capitalizeFirstCharacter(name()) + "();";
    }
}
