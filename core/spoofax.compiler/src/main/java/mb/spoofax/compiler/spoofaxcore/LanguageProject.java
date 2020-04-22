package mb.spoofax.compiler.spoofaxcore;

import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.Coordinate;
import mb.spoofax.compiler.util.GradleProject;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface LanguageProject extends Serializable {
    class Builder extends ImmutableLanguageProject.Builder {}

    static Builder builder() {
        return new Builder();
    }


    @Value.Default default String defaultProjectSuffix() {
        return "";
    }

    @Value.Default default GradleProject project() {
        final String artifactId = shared().defaultArtifactId() + defaultProjectSuffix();
        return GradleProject.builder()
            .coordinate(Coordinate.of(shared().defaultGroupId(), artifactId, shared().defaultVersion()))
            .baseDirectory(shared().baseDirectory().appendSegment(artifactId))
            .build();
    }

    @Value.Default default String packageId() {
        return shared().defaultBasePackageId() + defaultProjectSuffix();
    }

    @Value.Default default String packagePath() {
        return Conversion.packageIdToPath(packageId());
    }


    Shared shared();
}
