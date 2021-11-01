package mb.cfg.metalang;

import mb.cfg.CompileLanguageSpecificationShared;
import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import org.immutables.value.Value;

import java.io.Serializable;

/**
 * Configuration for the ESV meta-language in the context of CFG.
 */
@Value.Immutable
public interface CfgEsvConfig extends Serializable {
    class Builder extends ImmutableCfgEsvConfig.Builder {
        public static ResourcePath getDefaultMainSourceDirectory(ResourcePath rootDirectory) {
            return rootDirectory.appendRelativePath("src");
        }

        public static ResourcePath getDefaultMainFile(ResourcePath rootDirectory) {
            return getDefaultMainSourceDirectory(rootDirectory).appendRelativePath("main.esv");
        }

        public static boolean getDefaultIncludeLibSpoofax2Exports(CompileLanguageSpecificationShared shared) {
            return shared.includeLibSpoofax2Exports();
        }

        public static ResourcePath getDefaultLibSpoofax2UnarchiveDirectory(CompileLanguageSpecificationShared shared) {
            return shared.libSpoofax2UnarchiveDirectory();
        }
    }

    static Builder builder() { return new Builder(); }


    default ResourcePath rootDirectory() {
        return compileLanguageShared().languageProject().project().baseDirectory();
    }

    @Value.Default default CfgEsvSource source() {
        return CfgEsvSource.files(
            Builder.getDefaultMainSourceDirectory(rootDirectory()),
            Builder.getDefaultMainFile(rootDirectory()),
            ListView.of(),
            Builder.getDefaultIncludeLibSpoofax2Exports(compileLanguageShared()),
            Builder.getDefaultLibSpoofax2UnarchiveDirectory(compileLanguageShared())
        );
    }


    @Value.Default default String outputFileName() {
        return "editor.esv.af";
    }

    default ResourcePath outputFile() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            .appendRelativePath(outputFileName()) // Append the file name.
            ;
    }


    /// Automatically provided sub-inputs

    CompileLanguageSpecificationShared compileLanguageShared();


    default void syncTo(StylerLanguageCompiler.Input.Builder builder) {
        builder.packedEsvRelativePath(outputFileName());
    }
}
