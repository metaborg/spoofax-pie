package mb.spoofax.compiler.spoofax3.language;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.Conversion;
import mb.str.spoofax.task.StrategoCompileToJava;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class Spoofax3StrategoRuntimeLanguageCompiler implements TaskDef<Spoofax3StrategoRuntimeLanguageCompiler.Input, None> {
    private final StrategoCompileToJava strategoCompileToJava;

    @Inject public Spoofax3StrategoRuntimeLanguageCompiler(StrategoCompileToJava strategoCompileToJava) {
        this.strategoCompileToJava = strategoCompileToJava;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        // TODO: error when Stratego root directory does not exist.
        // TODO: error when a Stratego include directory does not exist.
        // TODO: error when Stratego main file does not exist.
        final StrategoCompileToJava.Args strategoCompileInput = new StrategoCompileToJava.Args(
            input.strategoRootDirectory(),
            input.strategoMainFile(),
            new ArrayList<>(input.strategoIncludeDirs()),
            new ArrayList<>(input.strategoBuiltinLibs()),
            null,
            //input.strategoCacheDir().orElse(null), // TODO: this makes the compiler crash
            input.strategoOutputDir(),
            input.strategoOutputJavaPackageId(),
            new ArrayList<>()
        );
        context.require(strategoCompileToJava, strategoCompileInput);
        return None.instance;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends Spoofax3StrategoRuntimeLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        @Value.Default default ResourcePath strategoRootDirectory() {
            return languageProject().project().srcMainDirectory().appendRelativePath("str");
        }

        @Value.Default default ResourcePath strategoMainFile() {
            return strategoRootDirectory().appendRelativePath("main.str");
        }

        @Value.Default default List<ResourcePath> strategoIncludeDirs() {
            final ArrayList<ResourcePath> strategoIncludeDirs = new ArrayList<>();
            strategoIncludeDirs.add(strategoRootDirectory());
            return strategoIncludeDirs;
        }

        @Value.Default default List<String> strategoBuiltinLibs() {
            final ArrayList<String> strategoBuiltinLibs = new ArrayList<>();
            strategoBuiltinLibs.add("stratego-lib");
            return strategoBuiltinLibs;
        }

        @Value.Default default Optional<ResourcePath> strategoCacheDir() {
            return Optional.of(languageProject().project().buildDirectory().appendRelativePath("stratego-cache"));
        }

        @Value.Default default ResourcePath strategoOutputDir() {
            return generatedJavaSourcesDirectory() // Generated Java sources directory, so that Gradle compiles the Java sources into classes.
                .appendRelativePath(strategoOutputJavaPackagePath()) // Append package path.
                ;
        }

        @Value.Default default String strategoOutputJavaPackageId() {
            return languageProject().packageId() + ".strategies";
        }

        default String strategoOutputJavaPackagePath() {
            return Conversion.packageIdToPath(strategoOutputJavaPackageId());
        }


        /// Automatically provided sub-inputs

        LanguageProject languageProject();

        ResourcePath generatedJavaSourcesDirectory();


        default void syncTo(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
            builder.addInteropRegisterersByReflection(strategoOutputJavaPackageId() + ".InteropRegisterer");
        }
    }
}
