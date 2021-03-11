package mb.spoofx.lwb.compiler.cfg.metalang;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageShared;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
public interface CompileEsvInput extends Serializable {
    class Builder extends ImmutableCompileEsvInput.Builder {}

    static Builder builder() { return new Builder(); }


    @Value.Default default ResourcePath esvRootDirectory() {
        return compileLanguageShared().languageProject().project().srcDirectory();
    }

    @Value.Default default ResourcePath esvMainFile() {
        return esvRootDirectory().appendRelativePath("main.esv");
    }

    List<ResourcePath> esvIncludeDirs();


    @Value.Default default String esvAtermFormatFileRelativePath() {
        return "editor.esv.af";
    }

    default ResourcePath esvAtermFormatOutputFile() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            .appendRelativePath(esvAtermFormatFileRelativePath()) // Append the relative path to the aterm format file.
            ;
    }


    /// Automatically provided sub-inputs

    CompileLanguageShared compileLanguageShared();


    default void syncTo(StylerLanguageCompiler.Input.Builder builder) {
        builder.packedEsvRelativePath(esvAtermFormatFileRelativePath());
    }
}
