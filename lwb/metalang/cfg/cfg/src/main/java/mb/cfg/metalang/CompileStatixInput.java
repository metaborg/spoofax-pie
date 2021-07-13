package mb.cfg.metalang;

import mb.cfg.CompileLanguageSpecificationShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
public interface CompileStatixInput extends Serializable {
    class Builder extends ImmutableCompileStatixInput.Builder {}

    static Builder builder() { return new Builder(); }


    default ResourcePath rootDirectory() {
        return compileLanguageShared().languageProject().project().baseDirectory();
    }

    @Value.Default default ResourcePath mainSourceDirectory() {
        return compileLanguageShared().languageProject().project().srcDirectory();
    }

    @Value.Default default ResourcePath mainFile() {
        return mainSourceDirectory().appendRelativePath("main.stx");
    }

    List<ResourcePath> includeDirectories();


    @Value.Default default boolean enableSdf3SignatureGen() {
        return false;
    }


    default ResourcePath outputDirectory() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            ;
    }


    /// Automatically provided sub-inputs

    CompileLanguageSpecificationShared compileLanguageShared();


    default void syncTo(ConstraintAnalyzerLanguageCompiler.Input.Builder builder) {
        builder.enableNaBL2(false);
        builder.enableStatix(true);
    }
}
