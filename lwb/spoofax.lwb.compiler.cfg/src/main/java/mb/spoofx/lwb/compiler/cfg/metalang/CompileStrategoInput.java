package mb.spoofx.lwb.compiler.cfg.metalang;

import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageShared;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Value.Immutable
public interface CompileStrategoInput extends Serializable {
    class Builder extends ImmutableCompileStrategoInput.Builder {}

    static Builder builder() { return new Builder(); }


    @Value.Default default ResourcePath strategoRootDirectory() {
        return compileLanguageShared().languageProject().project().srcDirectory();
    }

    @Value.Default default ResourcePath strategoMainFile() {
        return strategoRootDirectory().appendRelativePath("main.str");
    }

    List<ResourcePath> strategoIncludeDirs();

    @Value.Default default List<String> strategoBuiltinLibs() {
        final ArrayList<String> strategoBuiltinLibs = new ArrayList<>();
        strategoBuiltinLibs.add("stratego-lib");
        strategoBuiltinLibs.add("stratego-gpp");
        return strategoBuiltinLibs;
    }

    @Value.Default default ResourcePath strategoCacheDir() {
        return compileLanguageShared().languageProject().project().buildDirectory().appendRelativePath("stratego-cache");
    }

    @Value.Default default ResourcePath strategoOutputDir() {
        return compileLanguageShared().generatedJavaSourcesDirectory() // Generated Java sources directory, so that Gradle compiles the Java sources into classes.
            .appendRelativePath(strategoOutputJavaPackagePath()) // Append package path.
            ;
    }

    @Value.Default default String strategoOutputJavaPackageId() {
        return compileLanguageShared().languageProject().packageId() + ".strategies";
    }

    default String strategoOutputJavaPackagePath() {
        return Conversion.packageIdToPath(strategoOutputJavaPackageId());
    }

    default ResourcePath strategoOutputJavaInteropRegistererFile() {
        return strategoOutputDir().appendRelativePath("InteropRegisterer.java");
    }

    default ResourcePath strategoOutputJavaMainFile() {
        return strategoOutputDir().appendRelativePath("Main.java");
    }


    default ListView<ResourcePath> javaSourceFiles() {
        return ListView.of(
            strategoOutputJavaInteropRegistererFile(),
            strategoOutputJavaMainFile()
        );
    }


    /// Automatically provided sub-inputs

    CompileLanguageShared compileLanguageShared();


    default void syncTo(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
        builder.addStrategyPackageIds(strategoOutputJavaPackageId());
        builder.addInteropRegisterersByReflection(strategoOutputJavaPackageId() + ".InteropRegisterer");
    }
}
