package mb.cfg.metalang;

import mb.cfg.CompileMetaLanguageSourcesShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.ExportsCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import org.immutables.value.Value;

import java.io.Serializable;

/**
 * Configuration for ESV in the context of CFG.
 */
@Value.Immutable
public interface CfgEsvConfig extends Serializable {
    String exportsId = "ESV";


    class Builder extends ImmutableCfgEsvConfig.Builder {}

    static Builder builder() {return new Builder();}


    @Value.Default default CfgEsvSource source() {
        return CfgEsvSource.files(CfgEsvSource.Files.builder()
            .compileMetaLanguageSourcesShared(compileMetaLanguageSourcesShared())
            .build()
        );
    }


    @Value.Default default String outputFileName() {
        return "editor.esv.af";
    }

    default ResourcePath outputFile() {
        return compileMetaLanguageSourcesShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
            .appendRelativePath(compileMetaLanguageSourcesShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            .appendRelativePath(outputFileName()) // Append the file name.
            ;
    }


    /// Automatically provided sub-inputs

    CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared();


    default void syncTo(StylerLanguageCompiler.Input.Builder builder) {
        builder.packedEsvRelativePath(outputFileName());
    }

    default void syncTo(ExportsCompiler.Input.Builder builder) {
        source().getFiles().ifPresent(files -> files.exportDirectories().forEach(exportDirectory -> builder.addDirectoryExport(exportsId, exportDirectory)));
    }
}
