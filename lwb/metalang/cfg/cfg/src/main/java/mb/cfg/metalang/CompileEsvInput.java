package mb.cfg.metalang;

import mb.cfg.CompileLanguageShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
public interface CompileEsvInput extends Serializable {
    class Builder extends ImmutableCompileEsvInput.Builder {}

    static Builder builder() { return new Builder(); }


    default ResourcePath rootDirectory() {
        return compileLanguageShared().languageProject().project().baseDirectory();
    }

    @Value.Default default ResourcePath mainSourceDirectory() {
        return rootDirectory().appendRelativePath("src");
    }

    @Value.Default default ResourcePath mainFile() {
        return mainSourceDirectory().appendRelativePath("main.esv");
    }

    List<ResourcePath> includeDirectories();


    @Value.Default default String atermFormatFileRelativePath() {
        return "editor.esv.af";
    }

    default ResourcePath atermFormatOutputFile() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            .appendRelativePath(atermFormatFileRelativePath()) // Append the relative path to the aterm format file.
            ;
    }


    /// Automatically provided sub-inputs

    CompileLanguageShared compileLanguageShared();


    default void syncTo(StylerLanguageCompiler.Input.Builder builder) {
        builder.packedEsvRelativePath(atermFormatFileRelativePath());
    }
}
