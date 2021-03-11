package mb.spoofx.lwb.compiler.cfg.metalang;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageShared;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
public interface CompileStatixInput extends Serializable {
    class Builder extends ImmutableCompileStatixInput.Builder {}

    static Builder builder() { return new Builder(); }


    @Value.Default default ResourcePath statixRootDirectory() {
        return compileLanguageShared().languageProject().project().srcDirectory();
    }

    @Value.Default default ResourcePath statixMainFile() {
        return statixRootDirectory().appendRelativePath("main.stx");
    }

    List<ResourcePath> statixIncludeDirs();


    default ResourcePath statixOutputDirectory() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            ;
    }


    /// Automatically provided sub-inputs

    CompileLanguageShared compileLanguageShared();


    default void syncTo(ConstraintAnalyzerLanguageCompiler.Input.Builder builder) {
        builder.enableNaBL2(false);
        builder.enableStatix(true);
    }
}
