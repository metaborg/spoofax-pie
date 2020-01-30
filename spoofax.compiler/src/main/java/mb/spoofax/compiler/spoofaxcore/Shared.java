package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.Coordinate;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.GradleRepository;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Value.Immutable
public interface Shared extends Serializable {
    class Builder extends ImmutableShared.Builder implements BuilderBase {
        public Builder withPersistentProperties(Properties properties) {
            with(properties, "defaultClassPrefix", this::defaultClassPrefix);
            with(properties, "defaultGroupId", this::defaultGroupId);
            with(properties, "defaultArtifactId", this::defaultArtifactId);
            with(properties, "defaultVersion", this::defaultVersion);
            with(properties, "defaultBasePackageId", this::defaultBasePackageId);
            return this;
        }
    }

    static Builder builder() {
        return new Builder();
    }


    /// Configuration.

    String name();

    ResourcePath baseDirectory();


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

    @Value.Default default String defaultBasePackageId() {
        return Conversion.nameToJavaPackageId(name());
    }


    /// Root project

    @Value.Default default GradleProject rootProject() {
        final String artifactId = defaultArtifactId();
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .baseDirectory(baseDirectory())
            .build();
    }


    /// Language project

    @Value.Default default String defaultLanguageProjectSuffix() {
        return "";
    }

    @Value.Default default GradleProject languageProject() {
        final String artifactId = defaultArtifactId() + defaultLanguageProjectSuffix();
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .baseDirectory(baseDirectory().appendSegment(artifactId))
            .build();
    }

    @Value.Default default String languageProjectPackage() {
        return defaultBasePackageId() + defaultLanguageProjectSuffix();
    }

    default String languageProjectPackagePath() {
        return Conversion.packageIdToPath(languageProjectPackage());
    }


    /// Adapter project

    @Value.Default default String defaultAdapterProjectSuffix() {
        return ".spoofax";
    }

    @Value.Default default GradleProject adapterProject() {
        final String artifactId = defaultArtifactId() + defaultAdapterProjectSuffix();
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .baseDirectory(baseDirectory().appendSegment(artifactId))
            .build();
    }

    @Value.Default default String adapterProjectPackage() {
        return defaultBasePackageId() + defaultAdapterProjectSuffix();
    }

    @Value.Default default String adapterProjectTaskPackage() {
        return adapterProjectPackage() + ".task";
    }

    @Value.Default default String adapterProjectCommandPackage() {
        return adapterProjectPackage() + ".command";
    }


    /// CLI project

    @Value.Default default String defaultCliProjectSuffix() {
        return ".cli";
    }

    @Value.Default default GradleProject cliProject() {
        final String artifactId = defaultArtifactId() + defaultCliProjectSuffix();
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .baseDirectory(baseDirectory().appendSegment(artifactId))
            .build();
    }

    @Value.Default default String cliProjectPackage() {
        return defaultBasePackageId() + defaultCliProjectSuffix();
    }


    /// Eclipse externaldeps project

    @Value.Default default String defaultEclipseExternaldepsProjectSuffix() {
        return ".eclipse.externaldeps";
    }

    @Value.Default default GradleProject eclipseExternaldepsProject() {
        final String artifactId = defaultArtifactId() + defaultEclipseExternaldepsProjectSuffix();
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .baseDirectory(baseDirectory().appendSegment(artifactId))
            .build();
    }

    @Value.Default default String eclipseExternaldepsProjectPackage() {
        return defaultBasePackageId() + defaultEclipseExternaldepsProjectSuffix();
    }


    /// Eclipse project

    @Value.Default default String defaultEclipseProjectSuffix() {
        return ".eclipse";
    }

    @Value.Default default GradleProject eclipseProject() {
        final String artifactId = defaultArtifactId() + defaultEclipseProjectSuffix();
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .baseDirectory(baseDirectory().appendSegment(artifactId))
            .build();
    }

    @Value.Default default String eclipseProjectPackage() {
        return defaultBasePackageId() + defaultEclipseProjectSuffix();
    }


    /// IntelliJ project

    @Value.Default default String defaultIntellijProjectSuffix() {
        return ".intellij";
    }

    @Value.Default default GradleProject intellijProject() {
        final String artifactId = defaultArtifactId() + defaultIntellijProjectSuffix();
        return GradleProject.builder()
            .coordinate(defaultGroupId(), artifactId, defaultVersion())
            .baseDirectory(baseDirectory().appendSegment(artifactId))
            .build();
    }

    @Value.Default default String intellijPackage() {
        return defaultBasePackageId() + defaultIntellijProjectSuffix();
    }


    // Repositories

    @Value.Default default GradleRepository metaborgReleasesRepository() {
        return GradleRepository.maven("https://artifacts.metaborg.org/content/repositories/releases/");
    }

    @Value.Default default GradleRepository metaborgSnapshotsRepository() {
        return GradleRepository.maven("https://artifacts.metaborg.org/content/repositories/snapshots/");
    }

    @Value.Default default GradleRepository metaborgMavenCentralMirrorRepository() {
        return GradleRepository.maven("https://artifacts.metaborg.org/content/repositories/central/");
    }

    @Value.Default default GradleRepository useTheSourceRepository() {
        return GradleRepository.maven("http://nexus.usethesource.io/content/repositories/public/");
    }

    @Value.Default default List<GradleRepository> defaultRepositories() {
        final ArrayList<GradleRepository> repositories = new ArrayList<>();
        repositories.add(useTheSourceRepository());
        return repositories;
    }

    @Value.Default default List<GradleRepository> defaultPluginRepositories() {
        final ArrayList<GradleRepository> repositories = new ArrayList<>();
        repositories.add(metaborgReleasesRepository());
        repositories.add(metaborgSnapshotsRepository());
        repositories.add(metaborgMavenCentralMirrorRepository());
        repositories.add(GradleRepository.mavenCentral());
        repositories.add(GradleRepository.jcenter());
        repositories.add(GradleRepository.gradlePluginPortal());
        return repositories;
    }


    /// Dependencies and versions

    /// Metaborg Gradle configuration plugin

    @Value.Default default String metaborgGradleConfigVersion() {
        return "0.3.12";
    }

    /// Metaborg Coronium version

    @Value.Default default String metaborgCoroniumVersion() {
        return "0.1.8";
    }

    /// BND plugin version

    @Value.Default default String bndPluginVersion() {
        return "4.3.1";
    }

    /// IntelliJ Gradle plugin version

    @Value.Default default String intellijGradlePluginVersion() {
        return "0.4.15";
    }

    /// IntelliJ Idea version

    @Value.Default default String intellijIdeaVersion() {
        return "2019.3.2";
    }


    /// Checker framework

    @Value.Default default String checkerFrameworkVersion() {
        return "3.0.0";
    }

    @Value.Default default GradleDependency checkerFrameworkQualifiersDep() {
        return GradleDependency.module(Coordinate.of("org.checkerframework", "checker-qual-android", checkerFrameworkVersion()));
    }


    /// Dagger

    @Value.Default default String daggerVersion() {
        return "2.25.2";
    }

    @Value.Default default GradleDependency daggerDep() {
        return GradleDependency.module(Coordinate.of("com.google.dagger", "dagger", daggerVersion()));
    }

    @Value.Default default GradleDependency daggerCompilerDep() {
        return GradleDependency.module(Coordinate.of("com.google.dagger", "dagger-compiler", daggerVersion()));
    }


    /// SLF4J

    @Value.Default default String slf4jVersion() {
        return "1.7.29";
    }

    @Value.Default default GradleDependency slf4jSimpleDep() {
        return GradleDependency.module(Coordinate.of("org.slf4j", "slf4j-simple", slf4jVersion()));
    }


    /// Metaborg log

    @Value.Default default String metaborgLogVersion() {
        return "0.3.0";
    }

    @Value.Default default GradleDependency logApiDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "log.api", metaborgLogVersion()));
    }

    @Value.Default default GradleDependency logBackendSLF4JDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "log.backend.slf4j", metaborgLogVersion()));
    }


    /// Metaborg resource

    @Value.Default default String metaborgResourceVersion() {
        return "0.4.3";
    }

    @Value.Default default GradleDependency resourceDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "resource", metaborgResourceVersion()));
    }


    /// Spoofax 2.x

    @Value.Default default String spoofax2xVersion() {
        return "2.6.0-SNAPSHOT";
    }

    @Value.Default default GradleDependency orgStrategoXTStrjDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "org.strategoxt.strj", spoofax2xVersion()));
    }

    @Value.Default default GradleDependency strategoXTMinJarDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "strategoxt-min-jar", spoofax2xVersion()));
    }


    /// PIE

    @Value.Default default String pieVersion() {
        return "develop-SNAPSHOT";
    }

    @Value.Default default GradleDependency pieApiDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "pie.api", pieVersion()));
    }

    @Value.Default default GradleDependency pieRuntimeDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "pie.runtime", pieVersion()));
    }

    @Value.Default default GradleDependency pieDaggerDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "pie.dagger", pieVersion()));
    }


    /// Spoofax-PIE

    @Value.Default default String spoofaxPieVersion() {
        return "develop-SNAPSHOT";
    }

    @Value.Default default GradleDependency commonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency jsglrCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "jsglr.common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency jsglr1CommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "jsglr1.common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency jsglr2CommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "jsglr2.common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency esvCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "esv.common", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency strategoCommonDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "stratego.common", spoofaxPieVersion()));
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

    @Value.Default default GradleDependency spoofaxCompilerInterfacesDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.compiler.interfaces", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency spoofaxCoreDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.core", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency spoofaxCliDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.cli", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency spoofaxEclipseDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.eclipse", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency spoofaxEclipseExternaldepsDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.eclipse.externaldeps", spoofaxPieVersion()));
    }

    @Value.Default default GradleDependency spoofaxIntellijDep() {
        return GradleDependency.module(Coordinate.of("org.metaborg", "spoofax.intellij", spoofaxPieVersion()));
    }


    default void savePersistentProperties(Properties properties) {
        properties.setProperty("defaultClassPrefix", defaultClassPrefix());
        properties.setProperty("defaultGroupId", defaultGroupId());
        properties.setProperty("defaultArtifactId", defaultArtifactId());
        properties.setProperty("defaultVersion", defaultVersion());
        properties.setProperty("defaultBasePackageId", defaultBasePackageId());
        properties.setProperty("spoofaxPieVersion", spoofaxPieVersion());
    }

    @Value.Check default void check() {
        // TODO: validate that classSuffix is a valid Java identifier
        // TODO: validate that defaultArtifactId is a valid Java package identifier
        // TODO: validate that defaultPackageId is a valid Java package identifier
    }
}
