package mb.cfg.metalang;

import mb.cfg.CompileMetaLanguageSourcesShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.ExportsCompiler;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import org.immutables.value.Value;

import java.io.Serializable;

/**
 * Configuration for Statix in the context of CFG.
 */
@Value.Immutable
public interface CfgStatixConfig extends Serializable {
    String exportsId = "Statix";


    class Builder extends ImmutableCfgStatixConfig.Builder {}

    static Builder builder() {return new Builder();}


    @Value.Default default CfgStatixSource source() {
        return CfgStatixSource.files(CfgStatixSource.Files.builder()
            .compileMetaLanguageSourcesShared(compileMetaLanguageSourcesShared())
            .build()
        );
    }

    @Value.Default default ResourcePath generatedSourcesDirectory() {
        return compileMetaLanguageSourcesShared().generatedSourcesDirectory().appendRelativePath("statix");
    }


    default ResourcePath outputSpecAtermDirectory() {
        return compileMetaLanguageSourcesShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
            .appendRelativePath(compileMetaLanguageSourcesShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            ;
    }


    /// Automatically provided sub-inputs

    CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared();


    default void syncTo(ConstraintAnalyzerLanguageCompiler.Input.Builder builder) {
        builder.enableNaBL2(false);
        builder.enableStatix(true);
    }

    default void syncTo(ExportsCompiler.Input.Builder builder) {
        source().getFiles().ifPresent(files -> files.exportDirectories().forEach(exportDirectory -> builder.addDirectoryExport(exportsId, exportDirectory)));
    }
}
