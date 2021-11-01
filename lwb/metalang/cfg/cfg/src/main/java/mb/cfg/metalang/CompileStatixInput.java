package mb.cfg.metalang;

import mb.cfg.CompileLanguageSpecificationShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
public interface CompileStatixInput extends Serializable {
    class Builder extends ImmutableCompileStatixInput.Builder {
        public static ResourcePath getDefaultMainSourceDirectory(CompileLanguageSpecificationShared shared) {
            return shared.languageProject().project().srcDirectory();
        }

        public static ResourcePath getDefaultMainFile(CompileLanguageSpecificationShared shared) {
            return getDefaultMainSourceDirectory(shared).appendRelativePath("main.stx");
        }
    }

    static Builder builder() { return new Builder(); }


    @Value.Default default ResourcePath mainSourceDirectory() {
        return CompileStatixInput.Builder.getDefaultMainSourceDirectory(compileLanguageShared());
    }

    @Value.Default default ResourcePath mainFile() {
        return CompileStatixInput.Builder.getDefaultMainFile(compileLanguageShared());
    }

    List<ResourcePath> includeDirectories();

    @Value.Default default ResourcePath generatedSourcesDirectory() {
        return compileLanguageShared().generatedSourcesDirectory().appendRelativePath("statix");
    }

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
