package mb.cfg.metalang;

import mb.cfg.CompileLanguageSpecificationShared;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.Shared;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Value.Immutable
public interface CompileStrategoInput extends Serializable {
    class Builder extends ImmutableCompileStrategoInput.Builder implements BuilderBase {
        static final String propertiesPrefix = "stratego.";
        static final String languageStrategyAffix = propertiesPrefix + "languageStrategyAffix";

        public Builder withPersistentProperties(Properties properties) {
            with(properties, languageStrategyAffix, this::languageStrategyAffix);
            return this;
        }
    }

    static Builder builder() { return new Builder(); }


    default ResourcePath rootDirectory() {
        return compileLanguageShared().languageProject().project().baseDirectory();
    }

    @Value.Default default ResourcePath mainSourceDirectory() {
        return rootDirectory().appendRelativePath("src");
    }

    @Value.Default default ResourcePath mainFile() {
        return mainSourceDirectory().appendRelativePath("main.str2");
    }

    @Value.Default default String mainModule() {
        return "main";
    }

    List<ResourcePath> includeDirectories();

    @Value.Default default List<String> includeBuiltinLibraries() {
        final ArrayList<String> strategoBuiltinLibs = new ArrayList<>();
        strategoBuiltinLibs.add("stratego-gpp");
        strategoBuiltinLibs.add("libstratego-sglr");
        return strategoBuiltinLibs;
    }

    default ResourcePath strategoLibUnarchiveDirectory() {
        return compileLanguageShared().unarchiveDirectory().appendRelativePath("strategoLib");
    }

    @Value.Default default boolean includeLibSpoofax2Exports() {
        return compileLanguageShared().includeLibSpoofax2Exports();
    }

    @Value.Default default ResourcePath libSpoofax2UnarchiveDirectory() {
        return compileLanguageShared().libSpoofax2UnarchiveDirectory();
    }

    @Value.Default default boolean includeLibStatixExports() {
        return compileLanguageShared().includeLibStatixExports();
    }

    @Value.Default default ResourcePath libStatixUnarchiveDirectory() {
        return compileLanguageShared().libStatixUnarchiveDirectory();
    }

    @Value.Default default ResourcePath generatedSourcesDirectory() {
        return compileLanguageShared().generatedSourcesDirectory().appendRelativePath("stratego");
    }


    @Value.Default default ResourcePath cacheDirectory() {
        return compileLanguageShared().languageProject().project().buildDirectory().appendRelativePath("stratego-cache");
    }


    @Value.Default default boolean enableSdf3StatixExplicationGen() {
        return false;
    }


    @Value.Default default String languageStrategyAffix() {
        // TODO: convert to Stratego ID instead of Java ID.
        return Conversion.nameToJavaId(shared().name().toLowerCase());
    }


    @Value.Default default ResourcePath javaSourceFileOutputDir() {
        // Generated Java sources directory, so that Gradle compiles the Java sources into classes.
        return compileLanguageShared().generatedJavaSourcesDirectory();
    }

    @Value.Default default ResourcePath javaClassFileOutputDir() {
        return compileLanguageShared().languageProject().project().buildClassesDirectory();
    }

    @Value.Default default String outputJavaPackageId() {
        return compileLanguageShared().languageProject().packageId() + ".strategies";
    }

    default String outputJavaPackagePath() {
        return Conversion.packageIdToPath(outputJavaPackageId());
    }

    default String outputLibraryName() {
        return compileLanguageShared().languageProject().project().coordinate().artifactId();
    }

    default ResourcePath outputJavaInteropRegistererFile() {
        return javaSourceFileOutputDir().appendRelativePath("InteropRegisterer.java");
    }

    default ResourcePath outputJavaMainFile() {
        return javaSourceFileOutputDir().appendRelativePath("Main.java");
    }


    /// Automatically provided sub-inputs

    CompileLanguageSpecificationShared compileLanguageShared();

    Shared shared();


    default void savePersistentProperties(Properties properties) {
        properties.setProperty(Builder.languageStrategyAffix, languageStrategyAffix());
    }


    default void syncTo(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
        builder.addStrategyPackageIds(outputJavaPackageId());
        builder.addInteropRegisterersByReflection(outputJavaPackageId() + ".InteropRegisterer");
    }
}
