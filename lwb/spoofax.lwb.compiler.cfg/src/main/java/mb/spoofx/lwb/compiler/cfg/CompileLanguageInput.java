package mb.spoofx.lwb.compiler.cfg;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileEsvInput;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileSdf3Input;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileStatixInput;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileStrategoInput;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

@Value.Immutable
public interface CompileLanguageInput extends Serializable {
    class Builder extends ImmutableCompileLanguageInput.Builder {}

    static Builder builder() { return new Builder(); }


    /// Shared

    CompileLanguageShared compileLanguageShared();


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


    default void savePersistentProperties(Properties properties) {
        sdf3().ifPresent(i -> i.savePersistentProperties(properties));
    }


    default void syncTo(LanguageProjectCompilerInputBuilder builder) {
        sdf3().ifPresent(i -> i.syncTo(builder.parser));
        esv().ifPresent(i -> i.syncTo(builder.styler));
        statix().ifPresent(i -> i.syncTo(builder.constraintAnalyzer));
        stratego().ifPresent(i -> i.syncTo(builder.strategoRuntime));
    }
}
