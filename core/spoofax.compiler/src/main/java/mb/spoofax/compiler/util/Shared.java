package mb.spoofax.compiler.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Value.Immutable
public interface Shared extends Serializable {
    class Builder extends ImmutableShared.Builder implements BuilderBase {
        static final String propertiesPrefix = "shared.";
        static final String defaultClassPrefix = propertiesPrefix + "defaultClassPrefix";
        static final String defaultGroupId = propertiesPrefix + "defaultGroupId";
        static final String defaultArtifactId = propertiesPrefix + "defaultArtifactId";
        static final String defaultVersion = propertiesPrefix + "defaultVersion";
        static final String defaultPackageId = propertiesPrefix + "defaultPackageId";

        public Builder withPersistentProperties(Properties properties) {
            with(properties, defaultClassPrefix, this::defaultClassPrefix);
            with(properties, defaultGroupId, this::defaultGroupId);
            with(properties, defaultArtifactId, this::defaultArtifactId);
            with(properties, defaultVersion, this::defaultVersion);
            with(properties, defaultPackageId, this::defaultPackageId);
            return this;
        }
    }

    static Builder builder() {
        return new Builder();
    }


    /// Configuration.

    String name();


    @Value.Default default List<String> fileExtensions() {
        final ArrayList<String> list = new ArrayList<>();
        list.add(Conversion.nameToFileExtension(name()));
        return list;
    }


    @Value.Default default String defaultClassPrefix() {
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

    @Value.Default default String defaultPackageIdPrefix() {
        return "mb.";
    }

    @Value.Default default String defaultPackageId() {
        return defaultPackageIdPrefix() + Conversion.nameToJavaPackageId(name());
    }


    // Repositories

    @Value.Default default GradleRepository metaborgPublicRepository() {
        return GradleRepository.maven("https://artifacts.metaborg.org/content/groups/public/");
    }

    @Value.Default default List<GradleRepository> defaultRepositories() {
        final ArrayList<GradleRepository> repositories = new ArrayList<>();
        repositories.add(metaborgPublicRepository());
        return repositories;
    }

    @Value.Default default List<GradleRepository> defaultPluginRepositories() {
        final ArrayList<GradleRepository> repositories = new ArrayList<>();
        repositories.add(metaborgPublicRepository());
        return repositories;
    }


    /// Dependencies and versions

    /// Gradle plugins

    @Value.Default default String metaborgGradleConfigVersion() {
        return "0.3.21";
    }

    @Value.Default default String metaborgCoroniumVersion() {
        return "0.3.0";
    }

    @Value.Default default String bndPluginVersion() {
        return "5.0.1";
    }

    @Value.Default default String intellijGradlePluginVersion() {
        return "0.4.21";
    }


    /// Java dependencies

    /**
     * Gets the version properties provided by the build.
     */
    @Value.Auxiliary @Value.Default default Properties versionProperties() {
        final Properties properties = new Properties();
        final @Nullable InputStream inputStream = Shared.class.getClassLoader().getResourceAsStream("version.properties");
        if(inputStream != null) {
            try(final InputStream propertiesInputStream = inputStream) {
                properties.load(propertiesInputStream);
            } catch(IOException e) {
                // Ignore
            }
        }
        return properties;
    }

    /**
     * Gets the Spoofax 3 version, defaulting to the same version that was used to build this class.
     */
    @Value.Default default @Nullable String spoofax3Version() {
        return versionProperties().getProperty("spoofax3");
    }

    /**
     * Gets a dependency to the Spoofax 3 dependency constraints (org.metaborg:spoofax.depconstraints), which are used
     * to align the versions of dependencies used by Spoofax 3 languages.
     */
    @Value.Default default GradleDependency spoofaxDependencyConstraintsDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.depconstraints", spoofax3Version()));
    }


    /// Checker framework

    @Value.Default default GradleDependency checkerFrameworkQualifiersDep() {
        return GradleDependency.module(Coordinate.of("org.checkerframework", "checker-qual-android"));
    }

    /// Dagger

    @Value.Default default GradleDependency daggerDep() {
        return GradleDependency.module(Coordinate.of("com.google.dagger", "dagger"));
    }

    @Value.Default default GradleDependency daggerCompilerDep() {
        return GradleDependency.module(Coordinate.of("com.google.dagger", "dagger-compiler"));
    }

    /// SLF4J

    @Value.Default default GradleDependency slf4jSimpleDep() {
        return GradleDependency.module(Coordinate.of("org.slf4j", "slf4j-simple"));
    }

    /// Metaborg common

    @Value.Default default GradleDependency commonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "common"));
    }

    /// Metaborg log

    @Value.Default default GradleDependency logApiDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "log.api"));
    }

    @Value.Default default GradleDependency logBackendSLF4JDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "log.backend.slf4j"));
    }

    /// Metaborg resource

    @Value.Default default GradleDependency resourceDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "resource"));
    }

    /// Spoofax 2

    @Value.Default default GradleDependency strategoXTMinJarDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "strategoxt-min-jar"));
    }

    /// Spoofax 2 with devenv override

    @Value.Default default GradleDependency orgStrategoXTStrjDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg.devenv", "org.strategoxt.strj"));
    }

    /// PIE

    @Value.Default default GradleDependency pieApiDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "pie.api"));
    }

    @Value.Default default GradleDependency pieRuntimeDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "pie.runtime"));
    }

    /// Spoofax 3 core

    @Value.Default default GradleDependency atermCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "aterm.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency statixCodeCompletionDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "statix.codecompletion", spoofax3Version()));
    }

    @Value.Default default GradleDependency statixCodeCompletionPieDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "statix.codecompletion.pie", spoofax3Version()));
    }

    @Value.Default default GradleDependency jsglrCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "jsglr.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency jsglrPieDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "jsglr.pie", spoofax3Version()));
    }

    @Value.Default default GradleDependency jsglr1CommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "jsglr1.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency jsglr2CommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "jsglr2.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency esvCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "esv.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency strategoCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "stratego.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency strategoPieDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "stratego.pie", spoofax3Version()));
    }

    @Value.Default default GradleDependency constraintCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "constraint.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency constraintPieDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "constraint.pie", spoofax3Version()));
    }

    @Value.Default default GradleDependency nabl2CommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "nabl2.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency statixCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "statix.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency statixPieDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "statix.pie", spoofax3Version()));
    }

    @Value.Default default GradleDependency spoofax2CommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax2.common", spoofax3Version()));
    }

    @Value.Default default GradleDependency spoofaxCompilerInterfacesDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.compiler.interfaces", spoofax3Version()));
    }

    @Value.Default default GradleDependency spoofaxCoreDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.core", spoofax3Version()));
    }

    @Value.Default default GradleDependency spoofaxCliDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.cli", spoofax3Version()));
    }

    @Value.Default default GradleDependency spoofaxEclipseDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.eclipse", spoofax3Version()));
    }

    @Value.Default default GradleDependency toolingEclipseBundleDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "tooling.eclipsebundle", spoofax3Version()));
    }

    @Value.Default default GradleDependency spoofaxIntellijDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.intellij", spoofax3Version()));
    }

    @Value.Default default GradleDependency multilangDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "statix.multilang", spoofax3Version()));
    }

    @Value.Default default GradleDependency multilangEclipseDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "statix.multilang.eclipse", spoofax3Version()));
    }

    @Value.Default default GradleDependency sptApiDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spt.api", spoofax3Version()));
    }

    @Value.Default default GradleDependency tegoDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "tego", spoofax3Version()));
    }

    @Value.Default default GradleDependency tegoPieDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "tego.pie", spoofax3Version()));
    }


    /// Spoofax 3 metalib

    @Value.Default default GradleDependency strategolibDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "strategolib", spoofax3Version()));
    }

    @Value.Default default GradleDependency strategolibEclipseDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "strategolib.eclipse", spoofax3Version()));
    }


    default void savePersistentProperties(Properties properties) {
        properties.setProperty(Builder.defaultClassPrefix, defaultClassPrefix());
        properties.setProperty(Builder.defaultGroupId, defaultGroupId());
        properties.setProperty(Builder.defaultArtifactId, defaultArtifactId());
        properties.setProperty(Builder.defaultVersion, defaultVersion());
        properties.setProperty(Builder.defaultPackageId, defaultPackageId());
    }

    @Value.Check default void check() {
        // TODO: validate that classSuffix is a valid Java identifier
        // TODO: validate that defaultArtifactId is a valid Java package identifier
        // TODO: validate that defaultPackageId is a valid Java package identifier
    }
}
