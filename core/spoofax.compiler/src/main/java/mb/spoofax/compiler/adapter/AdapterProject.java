package mb.spoofax.compiler.adapter;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;

@Value.Immutable
public interface AdapterProject extends Serializable {
    class Builder extends ImmutableAdapterProject.Builder {
        public Builder withDefaultsFromParentDirectory(ResourcePath parentDirectory, Shared shared) {
            return withDefaults(parentDirectory.appendRelativePath(defaultArtifactId(shared)), shared);
        }

        public Builder withDefaults(ResourcePath baseDirectory, Shared shared) {
            final GradleProject gradleProject = GradleProject.builder()
                .coordinate(shared.defaultGroupId(), defaultArtifactId(shared), Optional.of(shared.defaultVersion()))
                .baseDirectory(baseDirectory)
                .build();
            return this
                .project(gradleProject)
                .packageId(defaultPackageId(shared))
                .shared(shared)
                ;
        }

        public static String defaultProjectSuffix() {
            return "";
        }

        public static String defaultArtifactId(Shared shared) {
            return shared.defaultArtifactId() + defaultProjectSuffix();
        }

        public static String defaultPackageId(Shared shared) {
            return shared.defaultPackageId() + defaultProjectSuffix();
        }


        public Builder withDefaultsSeparateProjectFromParentDirectory(ResourcePath parentDirectory, Shared shared) {
            return withDefaultsSeparateProject(parentDirectory.appendRelativePath(defaultSeparateArtifactId(shared)), shared);
        }

        public Builder withDefaultsSeparateProject(ResourcePath baseDirectory, Shared shared) {
            final GradleProject gradleProject = GradleProject.builder()
                .coordinate(shared.defaultGroupId(), defaultSeparateArtifactId(shared), Optional.of(shared.defaultVersion()))
                .baseDirectory(baseDirectory)
                .build();
            return this
                .project(gradleProject)
                .packageId(defaultSeparatePackageId(shared))
                .shared(shared)
                ;
        }

        public static String defaultSeparateProjectSuffix() {
            return ".spoofax";
        }

        public static String defaultSeparateArtifactId(Shared shared) {
            return shared.defaultArtifactId() + defaultSeparateProjectSuffix();
        }

        public static String defaultSeparatePackageId(Shared shared) {
            return shared.defaultPackageId() + defaultSeparateProjectSuffix();
        }
    }

    static Builder builder() {
        return new Builder();
    }


    /// Configuration

    GradleProject project();

    String packageId();

    default String packagePath() {
        return Conversion.packageIdToPath(packageId());
    }

    @Value.Default default String taskPackageId() {
        return packageId() + ".task";
    }

    default String taskPackagePath() {
        return Conversion.packageIdToPath(taskPackageId());
    }

    @Value.Default default String commandPackageId() {
        return packageId() + ".command";
    }


    @Value.Default default ResourcePath generatedJavaSourcesDirectory() {
        return project().buildGeneratedSourcesDirectory().appendRelativePath("adapter");
    }


    /// Kinds of classes (generated/extended/manual)

    @Value.Default default ClassKind classKind() {
        return ClassKind.Generated;
    }


    // Dagger resources scope

    @Value.Default default TypeInfo baseResourcesScope() {
        return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ResourcesScope");
    }

    Optional<TypeInfo> extendResourcesScope();

    default TypeInfo resourcesScope() {
        return extendResourcesScope().orElseGet(this::baseResourcesScope);
    }

    // Dagger Scope

    @Value.Default default TypeInfo baseScope() {
        return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Scope");
    }

    Optional<TypeInfo> extendScope();

    default TypeInfo scope() {
        return extendScope().orElseGet(this::baseScope);
    }

    // Dagger Qualifier

    @Value.Default default TypeInfo baseQualifier() {
        return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Qualifier");
    }

    Optional<TypeInfo> extendQualifier();

    default TypeInfo qualifier() {
        return extendQualifier().orElseGet(this::baseQualifier);
    }


    /// Automatically provided sub-inputs

    Shared shared();
}
