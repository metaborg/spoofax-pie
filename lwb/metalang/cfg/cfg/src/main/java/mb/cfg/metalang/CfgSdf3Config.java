package mb.cfg.metalang;

import mb.cfg.CompileLanguageSpecificationShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.ExportsCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import org.immutables.value.Value;

import java.io.Serializable;

/**
 * Configuration for SDF3 in the context of CFG.
 */
@Value.Immutable
public interface CfgSdf3Config extends Serializable {
    String exportsId = "SDF3";


    class Builder extends ImmutableCfgSdf3Config.Builder {
        public static ResourcePath getDefaultMainSourceDirectory(CompileLanguageSpecificationShared shared) {
            return shared.languageProject().project().srcDirectory();
        }

        public static ResourcePath getDefaultMainFile(ResourcePath mainSourceDirectory) {
            return mainSourceDirectory.appendRelativePath("start.sdf3");
        }
    }

    static Builder builder() {return new Builder();}


    @Value.Default default CfgSdf3Source source() {
        return CfgSdf3Source.files(CfgSdf3Source.Files.builder()
            .compileLanguageShared(compileLanguageShared())
            .build()
        );
    }

    @Value.Default default boolean createDynamicParseTable() {
        return false;
    }

    @Value.Default default boolean createDataDependentParseTable() {
        return false;
    }

    @Value.Default default boolean createLayoutSensitiveParseTable() {
        return false;
    }

    @Value.Default default boolean solveDeepConflictsInParseTable() {
        return true;
    }

    @Value.Default default boolean checkOverlapInParseTable() {
        return false;
    }

    @Value.Default default boolean checkPrioritiesInParseTable() {
        return false;
    }


    @Value.Default default String parseTableAtermFileRelativePath() {
        return "sdf.tbl";
    }

    default ResourcePath parseTableAtermOutputFile() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the parse table in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            .appendRelativePath(parseTableAtermFileRelativePath()) // Append the relative path to the parse table.
            ;
    }

    @Value.Default default String parseTablePersistedFileRelativePath() {
        return "sdf.bin";
    }

    default ResourcePath parseTablePersistedOutputFile() {
        return compileLanguageShared().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the parse table in the JAR file.
            .appendRelativePath(compileLanguageShared().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
            .appendRelativePath(parseTablePersistedFileRelativePath()) // Append the relative path to the parse table.
            ;
    }


    /// Automatically provided sub-inputs

    CompileLanguageSpecificationShared compileLanguageShared();


    default void syncTo(ParserLanguageCompiler.Input.Builder builder) {
        builder.parseTableAtermFileRelativePath(parseTableAtermFileRelativePath());
        builder.parseTablePersistedFileRelativePath(parseTablePersistedFileRelativePath());
    }

    default void syncTo(ExportsCompiler.Input.Builder builder) {
        source().getFiles().ifPresent(files -> files.exportDirectories().forEach(exportDirectory -> builder.addDirectoryExport(exportsId, exportDirectory)));
    }
}
