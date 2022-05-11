package mb.cfg.metalang;

import mb.cfg.CompileMetaLanguageSourcesShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.ExportsCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.BuilderBase;
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


    class Builder extends ImmutableCfgStrategoConfig.Builder implements BuilderBase {
        static final String propertiesPrefix = "stratego.";
        static final String languageStrategyAffix = propertiesPrefix + "languageStrategyAffix";

        public Builder withPersistentProperties(Properties properties) {
            with(properties, languageStrategyAffix, this::languageStrategyAffix);
            return this;
        }
    }

    static Builder builder() {return new Builder();}


    @Value.Default default CfgStrategoSource source() {
        return CfgStrategoSource.files(CfgStrategoSource.Files.builder()
            .compileMetaLanguageSourcesShared(compileLanguageShared())
            .build()
        );
    }


    @Value.Default default boolean enableSdf3StatixExplicationGen() {
        // TODO: move into source after CC lab.
        return false;
    }

    @Value.Default default String languageStrategyAffix() {
        // TODO: should go into CfgStrategoSource.Files? complicated due to persistent properties though...
        // TODO: convert to Stratego ID instead of Java ID.
        return Conversion.nameToJavaId(shared().name().toLowerCase());
    }


    @Value.Default default ResourcePath javaSourceFileOutputDirectory() {
        // Generated Java sources directory, so that Gradle compiles the Java sources into classes.
        return compileLanguageShared().generatedJavaSourcesDirectory();
    }

    @Value.Default default ResourcePath javaClassFileOutputDirectory() {
        return compileLanguageShared().languageProject().project().buildClassesDirectory();
    }

    @Value.Default default String outputJavaPackageId() {
        return compileLanguageShared().languageProject().packageId() + ".strategies";
    }

    default String outputJavaPackagePath() {
        return Conversion.packageIdToPath(outputJavaPackageId());
    }

    default String outputLibraryName() {
        return compileLanguageShared().languageProject().project().coordinate().artifactId;
    }

    default ResourcePath outputJavaInteropRegistererFile() {
        return javaSourceFileOutputDirectory().appendRelativePath("InteropRegisterer.java");
    }

    default ResourcePath outputJavaMainFile() {
        return javaSourceFileOutputDirectory().appendRelativePath("Main.java");
    }


    /// Automatically provided sub-inputs

    CompileMetaLanguageSourcesShared compileLanguageShared();

    Shared shared();


    default void savePersistentProperties(Properties properties) {
        properties.setProperty(Builder.languageStrategyAffix, languageStrategyAffix());
    }


    default void syncTo(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
        builder.addStrategyPackageIds(outputJavaPackageId());
        builder.addInteropRegisterersByReflection(outputJavaPackageId() + ".InteropRegisterer");
    }

    default void syncTo(ExportsCompiler.Input.Builder builder) {
        source().getFiles().exportDirectories().forEach(exportDirectory -> builder.addDirectoryExport(exportsId, exportDirectory));
    }
}
