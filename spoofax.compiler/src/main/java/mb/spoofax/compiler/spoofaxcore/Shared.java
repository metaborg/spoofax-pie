package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.Conversion;
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


    @Value.Default default String spoofaxPieVersion() {
        return "develop-SNAPSHOT";
    }

    @Value.Default default String spoofaxCoreVersion() {
        return "2.6.0-SNAPSHOT";
    }

    @Value.Default default String metaborgGradleConfigVersion() {
        return "0.3.9";
    }


    @Value.Default default JavaDependency resourceDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "resource", "develop-SNAPSHOT"));
    }

    @Value.Default default JavaDependency logApiDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "log.api", "develop-SNAPSHOT"));
    }

    @Value.Default default JavaDependency spoofaxCompilerInterfacesDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "spoofax.compiler.interfaces", spoofaxPieVersion()));
    }

    @Value.Default default JavaDependency commonDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "common", spoofaxPieVersion()));
    }

    @Value.Default default JavaDependency checkerFrameworkQualifiersDep() {
        return JavaDependency.module(Coordinate.of("org.checkerframework", "checker-qual-android", "2.6.0"));
    }


    @Value.Default default JavaDependency jsglr1CommonDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "jsglr1.common", spoofaxPieVersion()));
    }

    @Value.Default default JavaDependency esvCommonDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "esv.common", spoofaxPieVersion()));
    }

    @Value.Default default JavaDependency strategoCommonDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "stratego.common", spoofaxPieVersion()));
    }

    @Value.Default default JavaDependency orgStrategoXTStrjDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "org.strategoxt.strj", spoofaxCoreVersion()));
    }

    @Value.Default default JavaDependency strategoXTMinJarDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "strategoxt-min-jar", spoofaxCoreVersion()));
    }

    @Value.Default default JavaDependency constraintCommonDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "constraint.common", spoofaxPieVersion()));
    }

    @Value.Default default JavaDependency nabl2CommonDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "nabl2.common", spoofaxPieVersion()));
    }

    @Value.Default default JavaDependency statixCommonDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "statix.common", spoofaxPieVersion()));
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
