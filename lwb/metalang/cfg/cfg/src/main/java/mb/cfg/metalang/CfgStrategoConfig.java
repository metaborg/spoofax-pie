package mb.cfg.metalang;

import mb.cfg.CompileMetaLanguageSourcesShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.ExportsCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.Shared;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Properties;

/**
 * Configuration for Stratego in the context of CFG.
 */
@Value.Immutable
public interface CfgStrategoConfig extends Serializable {
    String exportsId = "Stratego";


    class Builder extends ImmutableCfgStrategoConfig.Builder {}

    static Builder builder() {return new Builder();}


    @Value.Default default CfgStrategoSource source() {
        return CfgStrategoSource.files(CfgStrategoSource.Files.builder()
            .compileMetaLanguageSourcesShared(compileMetaLanguageSourcesShared())
            .shared(shared())
            .build()
        );
    }


    @Value.Default default ResourcePath javaSourceFileOutputDirectory() {
        // Generated Java sources directory, so that Gradle compiles the Java sources into classes.
        return compileMetaLanguageSourcesShared().generatedJavaSourcesDirectory();
    }

    @Value.Default default ResourcePath javaClassFileOutputDirectory() {
        return compileMetaLanguageSourcesShared().languageProject().project().buildClassesDirectory();
    }

    @Value.Default default String outputJavaPackageId() {
        return compileMetaLanguageSourcesShared().languageProject().packageId() + ".strategies";
    }

    default String outputJavaPackagePath() {
        return Conversion.packageIdToPath(outputJavaPackageId());
    }

    default String outputLibraryName() {
        return compileMetaLanguageSourcesShared().languageProject().project().coordinate().artifactId;
    }

    default ResourcePath outputJavaInteropRegistererFile() {
        return javaSourceFileOutputDirectory().appendRelativePath("InteropRegisterer.java");
    }

    default ResourcePath outputJavaMainFile() {
        return javaSourceFileOutputDirectory().appendRelativePath("Main.java");
    }


    /// Automatically provided sub-inputs

    CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared();

    Shared shared();


    default void savePersistentProperties(Properties properties) {
        source().getFiles().savePersistentProperties(properties);
    }


    default void syncTo(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
        builder.addStrategyPackageIds(outputJavaPackageId());
        builder.addInteropRegisterersByReflection(outputJavaPackageId() + ".InteropRegisterer");
    }

    default void syncTo(ExportsCompiler.Input.Builder builder) {
        source().getFiles().syncTo(builder);
    }
}
