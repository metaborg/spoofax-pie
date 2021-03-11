package mb.spoofax.compiler.util;

import mb.resource.hierarchical.ResourcePath;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface TypeInfo extends Serializable {
    class Builder extends ImmutableTypeInfo.Builder {}

    static Builder builder() { return new Builder(); }

    static ImmutableTypeInfo of(String packageId, String id) {
        return ImmutableTypeInfo.of(packageId, id);
    }

    static ImmutableTypeInfo of(String qualifiedId) {
        final int dotIndex = qualifiedId.lastIndexOf('.');
        if(dotIndex != -1) {
            final String packageId = qualifiedId.substring(0, dotIndex);
            final String id = qualifiedId.substring(dotIndex + 1);
            return of(packageId, id);
        } else {
            return of("", qualifiedId);
        }
    }

    static ImmutableTypeInfo of(Class<?> cls) {
        if(cls.isAnonymousClass())
            throw new IllegalArgumentException("The class is anonymous, and therefore it is impossible to refer to it: " + cls.getName());
        Package aPackage = cls.getPackage();
        String packageId = aPackage != null ? aPackage.getName() : "";
        String id = cls.getCanonicalName();
        assert id.startsWith(packageId);
        if(!packageId.isEmpty()) id = id.substring(packageId.length() + 1);
        return ImmutableTypeInfo.of(packageId, id);
    }

    static ImmutableTypeInfo ofBoolean() {
        return ImmutableTypeInfo.of("", "boolean");
    }

    static ImmutableTypeInfo ofChar() {
        return ImmutableTypeInfo.of("", "char");
    }

    static ImmutableTypeInfo ofShort() {
        return ImmutableTypeInfo.of("", "short");
    }

    static ImmutableTypeInfo ofInt() {
        return ImmutableTypeInfo.of("", "int");
    }

    static ImmutableTypeInfo ofLong() {
        return ImmutableTypeInfo.of("", "long");
    }

    static ImmutableTypeInfo ofFloat() {
        return ImmutableTypeInfo.of("", "float");
    }

    static ImmutableTypeInfo ofDouble() {
        return ImmutableTypeInfo.of("", "double");
    }

    static ImmutableTypeInfo ofString() {
        return ImmutableTypeInfo.of("java.lang", "String");
    }


    @Value.Parameter String packageId();

    default String packagePath() {
        if(isPrimitive()) {
            throw new IllegalStateException("Cannot get package path, type '" + id() + "' is a primitive");
        }
        return Conversion.packageIdToPath(packageId());
    }

    default boolean isPrimitive() {
        return packageId().isEmpty();
    }

    @Value.Parameter String id();

    default String idAsCamelCase() {
        final String id = id();
        if(id.isEmpty()) {
            return id;
        }
        return id.substring(0, 1).toLowerCase() + id.substring(1);
    }

    default String qualifiedId() {
        if(isPrimitive()) {
            return id();
        } else {
            return packageId() + "." + id();
        }
    }

    default String nullableQualifiedId() {
        if(isPrimitive()) {
            return "@Nullable " + id();
        } else {
            return packageId() + ".@Nullable " + id();
        }
    }


    default String fileName() {
        if(isPrimitive()) {
            throw new IllegalStateException("Cannot get file name, type '" + id() + "' is a primitive");
        }
        return id() + ".java";
    }

    default String qualifiedPath() {
        if(isPrimitive()) {
            throw new IllegalStateException("Cannot get qualified path, type '" + id() + "' is a primitive");
        }
        return packagePath() + "/" + fileName();
    }

    default ResourcePath file(ResourcePath base) {
        if(isPrimitive()) {
            throw new IllegalStateException("Cannot get file, type '" + id() + "' is a primitive");
        }
        return base.appendRelativePath(qualifiedPath());
    }


    default String asVariableId() {
        return Conversion.classIdToVariableId(id());
    }


    default TypeInfo appendToId(String append) {
        return TypeInfo.of(this.packageId(), this.id() + append);
    }
}
