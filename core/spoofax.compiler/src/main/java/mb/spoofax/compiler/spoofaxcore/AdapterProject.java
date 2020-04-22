package mb.spoofax.compiler.spoofaxcore;

import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.Coordinate;
import mb.spoofax.compiler.util.GradleProject;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface AdapterProject extends Serializable {
    class Builder extends ImmutableAdapterProject.Builder {}

    static Builder builder() {
        return new Builder();
    }


    @Value.Default default String defaultProjectSuffix() {
        return ".spoofax";
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


    Shared shared();
}
