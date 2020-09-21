package mb.spoofax.compiler.language;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.GradleProject;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;

@Value.Immutable
public interface LanguageProject extends Serializable {
    class Builder extends ImmutableLanguageProject.Builder {
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
    }

    static Builder builder() {
        return new Builder();
    }


    GradleProject project();

    String packageId();


    @Value.Default default String packagePath() {
        return Conversion.packageIdToPath(packageId());
    }

    @Value.Default default ResourcePath generatedJavaSourcesDirectory() {
        return project().buildGeneratedSourcesDirectory().appendRelativePath("language");
    }
}
