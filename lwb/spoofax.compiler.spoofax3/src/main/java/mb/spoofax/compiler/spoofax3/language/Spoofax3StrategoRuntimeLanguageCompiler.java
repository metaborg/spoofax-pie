package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.Conversion;
import mb.str.spoofax.task.StrategoCheckMulti;
import mb.str.spoofax.task.StrategoCompileToJava;
import mb.str.spoofax.util.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Value.Enclosing
public class Spoofax3StrategoRuntimeLanguageCompiler implements TaskDef<Spoofax3StrategoRuntimeLanguageCompiler.Input, Result<KeyedMessages, StrategoCompilerException>> {
    private final StrategoCheckMulti strategoCheckMulti;
    private final StrategoCompileToJava strategoCompileToJava;

    @Inject public Spoofax3StrategoRuntimeLanguageCompiler(
        StrategoCheckMulti strategoCheckMulti,
        StrategoCompileToJava strategoCompileToJava
    ) {
        this.strategoCheckMulti = strategoCheckMulti;
        this.strategoCompileToJava = strategoCompileToJava;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, StrategoCompilerException> exec(ExecContext context, Input input) throws Exception {
        // Check main file, root directory, and include directories.
        final HierarchicalResource mainFile = context.require(input.strategoMainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(StrategoCompilerException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource rootDirectory = context.require(input.strategoRootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(StrategoCompilerException.rootDirectoryFail(rootDirectory.getPath()));
        }
        for(ResourcePath includeDirectory : input.strategoIncludeDirs()) {
            if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
                return Result.ofErr(StrategoCompilerException.includeDirectoryFail(includeDirectory));
            }
        }

        // Check Stratego source files.
        final @Nullable KeyedMessages messages = context.require(strategoCheckMulti.createTask(
            new StrategoCheckMulti.Input(rootDirectory.getPath(), StrategoUtil.createResourceWalker(), StrategoUtil.createResourceMatcher())
        ));
        if(messages.containsError()) {
            return Result.ofErr(StrategoCompilerException.checkFail(messages));
        }

        // Compile Stratego sources to Java sources.
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
        final Result<None, ?> compileResult = context.require(strategoCompileToJava, strategoCompileInput);
        if(compileResult.isErr()) {
            return Result.ofErr(StrategoCompilerException.compilerFail(compileResult.getErr()));
        }

        return Result.ofOk(messages);
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

        @Value.Default default ResourcePath strategoCacheDir() {
            return languageProject().project().buildDirectory().appendRelativePath("stratego-cache");
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
