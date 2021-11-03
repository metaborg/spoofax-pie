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

        public static ResourcePath getDefaultMainFile(CompileLanguageSpecificationShared shared) {
            return getDefaultMainSourceDirectory(shared).appendRelativePath("main.stx");
        }
    }

    static Builder builder() { return new Builder(); }


    @Value.Default default CfgStatixSource source() {
        return CfgStatixSource.files(
            CfgStatixConfig.Builder.getDefaultMainSourceDirectory(compileLanguageShared()),
            CfgStatixConfig.Builder.getDefaultMainFile(compileLanguageShared()),
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
