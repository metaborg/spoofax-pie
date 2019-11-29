package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.Coordinate;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Properties;

@Value.Immutable
public interface Shared extends Serializable {
    class Builder extends ImmutableShared.Builder implements BuilderBase {
        public Builder withPersistentProperties(Properties properties) {
            with(properties, "classSuffix", this::classSuffix);
            with(properties, "defaultGroupId", this::defaultGroupId);
            with(properties, "defaultArtifactId", this::defaultArtifactId);
            with(properties, "defaultVersion", this::defaultVersion);
            with(properties, "basePackageId", this::basePackageId);
            with(properties, "spoofaxPieVersion", this::spoofaxPieVersion);
            return this;
        }
    }

    static Builder builder() {
        return new Builder();
    }


    String name();

    @Value.Default default String classSuffix() {
        return Conversion.nameToJavaId(name());
    }

    @Value.Default default String defaultGroupId() {
        return "org.metaborg";
    }

    @Value.Default default String defaultArtifactId() {
        return Conversion.nameToJavaPackageId(name());
    }

    @Value.Default default String defaultVersion() {
        return "0.1.0";
    }

    @Value.Default default String basePackageId() {
        return Conversion.nameToJavaPackageId(name());
    }


    ResourcePath baseDirectory();


    @Value.Default default GradleProject rootProject() {
        final String artifactId = defaultArtifactId();
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .packageId(basePackageId())
            .baseDirectory(baseDirectory().appendSegment(artifactId))
            .build();
    }

    @Value.Default default GradleProject languageProject() {
        final String artifactId = defaultArtifactId() + ".lang";
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .packageId(basePackageId() + ".lang")
            .baseDirectory(baseDirectory().appendSegment(artifactId))
            .build();
    }

    @Value.Default default GradleProject adapterProject() {
        final String artifactId = defaultArtifactId() + ".spoofax";
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .packageId(basePackageId() + ".spoofax")
            .baseDirectory(baseDirectory().appendSegment(artifactId))
            .build();
    }


    @Value.Default default String spoofaxPieVersion() {
        return "develop-SNAPSHOT";
    }

    @Value.Default default String spoofaxCoreVersion() {
        return "2.6.0-SNAPSHOT";
    }

    @Value.Default default String metaborgGradleConfigVersion() {
        return "0.3.10";
    }


    @Value.Default default GradleDependency resourceDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "resource", "develop-SNAPSHOT"));
    }

    @Value.Default default GradleDependency logApiDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "log.api", "develop-SNAPSHOT"));
    }

    @Value.Default default GradleDependency spoofaxCompilerInterfacesDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.compiler.interfaces", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency commonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency checkerFrameworkQualifiersDep() {
        return GradleDependency.module(Coordinate.of("org.checkerframework", "checker-qual-android", "2.6.0"));
    }


    @Value.Default default GradleDependency jsglr1CommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "jsglr1.common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency esvCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "esv.common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency strategoCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "stratego.common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency orgStrategoXTStrjDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "org.strategoxt.strj", spoofaxCoreVersion()));
    }

    @Value.Default default GradleDependency strategoXTMinJarDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "strategoxt-min-jar", spoofaxCoreVersion()));
    }

    @Value.Default default GradleDependency constraintCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "constraint.common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency nabl2CommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "nabl2.common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency statixCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "statix.common", spoofaxPieVersion()));
    }


    default void savePersistentProperties(Properties properties) {
        properties.setProperty("classSuffix", classSuffix());
        properties.setProperty("defaultGroupId", defaultGroupId());
        properties.setProperty("defaultArtifactId", defaultArtifactId());
        properties.setProperty("defaultVersion", defaultVersion());
        properties.setProperty("defaultPackageId", basePackageId());
        properties.setProperty("spoofaxPieVersion", spoofaxPieVersion());
    }

    @Value.Check default void check() {
        // TODO: validate that classSuffix is a valid Java identifier
        // TODO: validate that defaultArtifactId is a valid Java package identifier
        // TODO: validate that defaultPackageId is a valid Java package identifier
    }
}
