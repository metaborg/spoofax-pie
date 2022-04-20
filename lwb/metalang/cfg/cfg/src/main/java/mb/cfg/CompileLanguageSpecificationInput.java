package mb.cfg;

import mb.cfg.metalang.CfgDynamixConfig;
import mb.cfg.metalang.CfgEsvConfig;
import mb.cfg.metalang.CfgSdf3Config;
import mb.cfg.metalang.CfgStatixConfig;
import mb.cfg.metalang.CfgStrategoConfig;
import mb.common.util.SetView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.core.CoordinateRequirement;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

@Value.Immutable
public interface CompileLanguageSpecificationInput extends Serializable {
    class Builder extends ImmutableCompileLanguageSpecificationInput.Builder {}

    static Builder builder() {return new Builder();}


    /// Shared

    Shared shared();

    CompileLanguageSpecificationShared compileLanguageShared();


    /// Sub-inputs

    Optional<CfgSdf3Config> sdf3();

    Optional<CfgEsvConfig> esv();

    Optional<CfgStatixConfig> statix();

    Optional<CfgDynamixConfig> dynamix();

    Optional<CfgStrategoConfig> stratego();

    List<Dependency> dependencies();

    default List<Dependency> allDependencies() {
        final ArrayList<Dependency> dependencies = new ArrayList<>(dependencies());
        final Function<String, Dependency> createDependency = (artifactId) ->
            new Dependency(DependencySource.coordinateRequirement(new CoordinateRequirement("org.metaborg", artifactId, shared().spoofax3Version())), SetView.of(DependencyKind.CompileTime));
        stratego().ifPresent(c -> {
            dependencies.add(createDependency.apply("strategolib"));
            dependencies.add(createDependency.apply("gpp"));
            if(c.source().getFiles().includeLibSpoofax2Exports()) {
                dependencies.add(createDependency.apply("libspoofax2"));
            }
            if(c.source().getFiles().includeLibStatixExports()) {
                dependencies.add(createDependency.apply("libstatix"));
            }
        });
        return dependencies;
    }


    /// Files information, known up-front for build systems with static dependencies such as Gradle.

    default ArrayList<ResourcePath> javaSourcePaths() {
        final ArrayList<ResourcePath> sourcePaths = new ArrayList<>();
        return sourcePaths;
    }

    default ArrayList<ResourcePath> javaSourceDirectoryPaths() {
        final ArrayList<ResourcePath> sourceDirectoryPaths = new ArrayList<>();
        // Add only as source directory path, as Java source files are directly passed from the Stratego compiler into
        // the Java compiler, to prevent hidden dependencies when Java source files are no longer provided. We still
        // need to add it as a source directory path so that the Java compiler can resolve packages (directories).
        sourceDirectoryPaths.add(compileLanguageShared().generatedJavaSourcesDirectory());
        return sourceDirectoryPaths;
    }

    default ArrayList<ResourcePath> javaSourceFiles() {
        final ArrayList<ResourcePath> javaSourceFiles = new ArrayList<>();
        // No static dependencies at the moment. All Java source files are passed from the Stratego compiler into the
        // Java compiler.
        return javaSourceFiles;
    }

    default ArrayList<ResourcePath> resourcePaths() {
        final ArrayList<ResourcePath> resourcePaths = new ArrayList<>();
        resourcePaths.add(compileLanguageShared().generatedResourcesDirectory());
        return resourcePaths;
    }


    default void savePersistentProperties(Properties properties) {
        stratego().ifPresent(i -> i.savePersistentProperties(properties));
    }


    default void syncTo(LanguageProjectCompilerInputBuilder builder) {
        sdf3().ifPresent(i -> i.syncTo(builder.parser));
        esv().ifPresent(i -> i.syncTo(builder.styler));
        statix().ifPresent(i -> i.syncTo(builder.constraintAnalyzer));
        stratego().ifPresent(i -> i.syncTo(builder.strategoRuntime));
        // todo: dynamix sync?
    }

    default void syncTo(AdapterProjectCompilerInputBuilder builder) {
        sdf3().ifPresent(i -> i.syncTo(builder.exports));
        esv().ifPresent(i -> i.syncTo(builder.exports));
        statix().ifPresent(i -> i.syncTo(builder.exports));
        stratego().ifPresent(i -> i.syncTo(builder.exports));
    }
}
