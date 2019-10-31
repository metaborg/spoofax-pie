package mb.spoofax.compiler.spoofaxcore;

import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.Conversion;
import org.immutables.value.Value;

import java.util.Properties;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface Shared {
    class Builder extends ImmutableShared.Builder implements BuilderBase {
        public Builder withPersistentProperties(Properties properties) {
            with(properties, "classSuffix", this::classSuffix);
            with(properties, "defaultGroupId", this::defaultGroupId);
            with(properties, "defaultArtifactId", this::defaultArtifactId);
            with(properties, "defaultVersion", this::defaultVersion);
            with(properties, "defaultPackageId", this::defaultPackageId);
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

    @Value.Default default String defaultPackageId() {
        return Conversion.nameToJavaPackageId(name());
    }


    @Value.Default default String spoofaxPieVersion() {
        return "develop-SNAPSHOT";
    }

    @Value.Default default JavaDependency commonDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "common", spoofaxPieVersion()));
    }

    @Value.Default default JavaDependency jsglr1CommonDep() {
        return JavaDependency.module(Coordinate.of("org.metaborg", "jsglr1.common", spoofaxPieVersion()));
    }

    @Value.Default default JavaDependency checkerFrameworkQualifiersDep() {
        return JavaDependency.module(Coordinate.of("org.checkerframework", "checker-qual-android", "2.6.0"));
    }


    default void savePersistentProperties(Properties properties) {
        properties.setProperty("classSuffix", classSuffix());
        properties.setProperty("defaultGroupId", defaultGroupId());
        properties.setProperty("defaultArtifactId", defaultArtifactId());
        properties.setProperty("defaultVersion", defaultVersion());
        properties.setProperty("defaultPackageId", defaultPackageId());
        properties.setProperty("spoofaxPieVersion", spoofaxPieVersion());
    }

    @Value.Check default void check() {
        // TODO: validate that classSuffix is a valid Java identifier
        // TODO: validate that defaultArtifactId is a valid Java package identifier
        // TODO: validate that defaultPackageId is a valid Java package identifier

    }
}
