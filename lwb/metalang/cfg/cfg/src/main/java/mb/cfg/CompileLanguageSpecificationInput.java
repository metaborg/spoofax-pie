package mb.cfg;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.cfg.metalang.CompileEsvInput;
import mb.cfg.metalang.CompileSdf3Input;
import mb.cfg.metalang.CompileStatixInput;
import mb.cfg.metalang.CompileStrategoInput;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

@Value.Immutable
public interface CompileLanguageSpecificationInput extends Serializable {
    class Builder extends ImmutableCompileLanguageSpecificationInput.Builder {}

    static Builder builder() { return new Builder(); }


    /// Shared

    CompileLanguageSpecificationShared compileLanguageShared();


    /// Sub-inputs

    Optional<CompileSdf3Input> sdf3();

    Optional<CompileEsvInput> esv();

    Optional<CompileStatixInput> statix();

    Optional<CompileStrategoInput> stratego();


    /// Files information, known up-front for build systems with static dependencies such as Gradle.

    default ArrayList<ResourcePath> javaSourcePaths() {
        final ArrayList<ResourcePath> sourcePaths = new ArrayList<>();
        sourcePaths.add(compileLanguageShared().generatedJavaSourcesDirectory());
        return sourcePaths;
    }

    default ArrayList<ResourcePath> javaSourceFiles() {
        final ArrayList<ResourcePath> javaSourceFiles = new ArrayList<>();
        stratego().ifPresent(i -> i.javaSourceFiles().addAllTo(javaSourceFiles));
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
    }
}
