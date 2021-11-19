package mb.cfg.metalang;

import mb.cfg.CompileLanguageSpecificationShared;
import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import org.immutables.value.Value;

import java.io.Serializable;

/**
 * Configuration for Statix in the context of CFG.
 */
@Value.Immutable
public interface CfgStatixConfig extends Serializable {
    class Builder extends ImmutableCfgStatixConfig.Builder {
        public static ResourcePath getDefaultMainSourceDirectory(CompileLanguageSpecificationShared shared) {
            return shared.languageProject().project().srcDirectory();
        }

        public static ResourcePath getDefaultMainFile(ResourcePath mainSourceDirectory) {
            return mainSourceDirectory.appendRelativePath("main.stx");
        }
    }

    static Builder builder() {return new Builder();}


    @Value.Default default CfgStatixSource source() {
        final ResourcePath mainSourceDirectory = Builder.getDefaultMainSourceDirectory(compileLanguageShared());
        return CfgStatixSource.files(
            mainSourceDirectory,
            CfgStatixConfig.Builder.getDefaultMainFile(mainSourceDirectory),
            ListView.of()
        );
    }

    @Value.Default default boolean enableSdf3SignatureGen() {
        // TODO: move into source after CC lab.
        return false;
    }

    @Value.Default default ResourcePath generatedSourcesDirectory() {
        return compileLanguageShared().generatedSourcesDirectory().appendRelativePath("statix");
    }


    default ResourcePath outputSpecAtermDirectory() {
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
