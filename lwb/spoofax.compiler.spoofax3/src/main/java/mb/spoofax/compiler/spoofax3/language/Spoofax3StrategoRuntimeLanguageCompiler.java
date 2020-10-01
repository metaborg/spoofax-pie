package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.libspoofax2.LibSpoofax2Exports;
import mb.libspoofax2.spoofax.LibSpoofax2Qualifier;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.Conversion;
import mb.str.spoofax.config.StrategoAnalyzeConfig;
import mb.str.spoofax.config.StrategoCompileConfig;
import mb.str.spoofax.config.StrategoConfigurator;
import mb.str.spoofax.task.StrategoCheckMulti;
import mb.str.spoofax.task.StrategoCompileToJava;
import mb.str.spoofax.util.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Value.Enclosing
public class Spoofax3StrategoRuntimeLanguageCompiler implements TaskDef<Spoofax3StrategoRuntimeLanguageCompiler.Args, Result<KeyedMessages, StrategoCompilerException>> {
    private final StrategoCheckMulti check;
    private final StrategoCompileToJava compileToJava;
    private final StrategoConfigurator configurator;

    private final ClassLoaderResource libSpoofax2DefinitionDir;

    @Inject public Spoofax3StrategoRuntimeLanguageCompiler(
        StrategoCheckMulti check,
        StrategoCompileToJava compileToJava,
        StrategoConfigurator configurator,
        @LibSpoofax2Qualifier("definition-dir") ClassLoaderResource libSpoofax2DefinitionDir
    ) {
        this.check = check;
        this.compileToJava = compileToJava;
        this.configurator = configurator;
        this.libSpoofax2DefinitionDir = libSpoofax2DefinitionDir;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, StrategoCompilerException> exec(ExecContext context, Args args) throws Exception {
        final Input input = args.input;

        // Check main file, root directory, and include directories.
        final HierarchicalResource mainFile = context.require(input.strategoMainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(StrategoCompilerException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource rootDirectory = context.require(input.strategoRootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(StrategoCompilerException.rootDirectoryFail(rootDirectory.getPath()));
        }
        for(ResourcePath includeDirectoryPath : input.strategoIncludeDirs()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(StrategoCompilerException.includeDirectoryFail(includeDirectoryPath));
            }
        }

        // Set compile configuration
        final ArrayList<ResourcePath> includeDirs = new ArrayList<>(input.strategoIncludeDirs());
        includeDirs.add(input.strategoRootDirectory());
        if(input.spoofax3LanguageProject().includeLibSpoofax2Exports()) {
            for(HierarchicalResource export : LibSpoofax2Exports.getStrategoExports(libSpoofax2DefinitionDir)) {
                includeDirs.add(export.getPath());
            }
        }
        final StrategoAnalyzeConfig analyzeConfig = new StrategoAnalyzeConfig(
            input.strategoRootDirectory(),
            input.strategoMainFile(),
            includeDirs,
            new ArrayList<>(input.strategoBuiltinLibs())
        );
        configurator.setAnalyzeConfig(input.strategoRootDirectory(), analyzeConfig);

        // Check Stratego source files.
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            new StrategoCheckMulti.Input(rootDirectory.getPath(), StrategoUtil.createResourceWalker(), StrategoUtil.createResourceMatcher(), args.originTasks)
        ));
        if(messages.containsError()) {
            return Result.ofErr(StrategoCompilerException.checkFail(messages));
        }

        // Compile Stratego sources to Java sources.
        final StrategoCompileConfig compileConfig = new StrategoCompileConfig(
            input.strategoRootDirectory(),
            input.strategoMainFile(),
            includeDirs,
            new ArrayList<>(input.strategoBuiltinLibs()),
            null, //input.strategoCacheDir().orElse(null), // TODO: this makes the compiler crash
            input.strategoOutputDir(),
            input.strategoOutputJavaPackageId()
        );
        final StrategoCompileToJava.Input compileInput = new StrategoCompileToJava.Input(
            compileConfig,
            args.originTasks
        );
        final Result<None, ?> compileResult = context.require(compileToJava, compileInput);
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
            return spoofax3LanguageProject().languageProject().project().srcMainDirectory().appendRelativePath("str");
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
            return spoofax3LanguageProject().languageProject().project().buildDirectory().appendRelativePath("stratego-cache");
        }

        @Value.Default default ResourcePath strategoOutputDir() {
            return spoofax3LanguageProject().generatedJavaSourcesDirectory() // Generated Java sources directory, so that Gradle compiles the Java sources into classes.
                .appendRelativePath(strategoOutputJavaPackagePath()) // Append package path.
                ;
        }

        @Value.Default default String strategoOutputJavaPackageId() {
            return spoofax3LanguageProject().languageProject().packageId() + ".strategies";
        }

        default String strategoOutputJavaPackagePath() {
            return Conversion.packageIdToPath(strategoOutputJavaPackageId());
        }


        /// Automatically provided sub-inputs

        Spoofax3LanguageProject spoofax3LanguageProject();


        default void syncTo(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
            builder.addInteropRegisterersByReflection(strategoOutputJavaPackageId() + ".InteropRegisterer");
        }
    }

    public static class Args implements Serializable {
        public final Input input;
        public final ArrayList<STask> originTasks;

        public Args(Input input, ArrayList<STask> originTasks) {
            this.input = input;
            this.originTasks = originTasks;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return input.equals(args.input) && originTasks.equals(args.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(input, originTasks);
        }

        @Override public String toString() {
            return "Args{" +
                "input=" + input +
                ", originTasks=" + originTasks +
                '}';
        }
    }
}
