package mb.spoofax.compiler.util;

import mb.resource.hierarchical.ResourcePath;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface ClassInfo extends Serializable {
    class Builder extends ImmutableClassInfo.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableClassInfo of(String packageId, String classId) {
        return ImmutableClassInfo.of(packageId, classId);
    }


    @Value.Parameter String packageId();

    default String packagePath() {
        return Conversion.packageIdToPath(packageId());
    }

    @Value.Parameter String id();

    default String qualifiedId() {
        return packageId() + "." + id();
    }


    default String fileName() {
        return id() + ".java";
    }

    default String qualifiedPath() {
        return packagePath() + "/" + fileName();
    }

    default ResourcePath file(ResourcePath base) {
        return base.appendRelativePath(qualifiedPath());
    }


    default String asVariableId() {
        return Conversion.classIdToVariableId(id());
    }
}
