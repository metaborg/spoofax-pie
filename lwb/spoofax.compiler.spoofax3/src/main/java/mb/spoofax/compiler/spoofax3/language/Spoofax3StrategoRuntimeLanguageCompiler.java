package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.libspoofax2.LibSpoofax2ClassLoaderResources;
import mb.libspoofax2.LibSpoofax2Exports;
import mb.libstatix.LibStatixClassLoaderResources;
import mb.libstatix.LibStatixExports;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.util.Conversion;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.config.StrategoCompileConfig;
import mb.str.config.StrategoConfigurator;
import mb.str.task.StrategoCheckMulti;
import mb.str.task.StrategoCompileToJava;
import mb.str.util.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Value.Enclosing
public class Spoofax3StrategoRuntimeLanguageCompiler implements TaskDef<Spoofax3StrategoRuntimeLanguageCompiler.Args, Result<KeyedMessages, StrategoCompilerException>> {
    private final StrategoCheckMulti check;
    private final StrategoCompileToJava compileToJava;
    private final StrategoConfigurator configurator;

    private final UnarchiveFromJar unarchiveFromJar;
    private final LibSpoofax2ClassLoaderResources libSpoofax2ClassLoaderResources;
    private final LibStatixClassLoaderResources libStatixClassLoaderResources;

    @Inject public Spoofax3StrategoRuntimeLanguageCompiler(
        StrategoCheckMulti check,
        StrategoCompileToJava compileToJava,
        StrategoConfigurator configurator,
        UnarchiveFromJar unarchiveFromJar,
        LibSpoofax2ClassLoaderResources libSpoofax2ClassLoaderResources,
        LibStatixClassLoaderResources libStatixClassLoaderResources
    ) {
        this.check = check;
        this.compileToJava = compileToJava;
        this.configurator = configurator;
        this.unarchiveFromJar = unarchiveFromJar;
        this.libSpoofax2ClassLoaderResources = libSpoofax2ClassLoaderResources;
        this.libStatixClassLoaderResources = libStatixClassLoaderResources;
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

        // Gather origins for provided Stratego files.
        final ArrayList<STask<?>> originTasks = new ArrayList<>(args.originTasks);

        // Gather include directories.
        final ArrayList<ResourcePath> includeDirs = new ArrayList<>();
        includeDirs.add(input.strategoRootDirectory());
        includeDirs.addAll(input.strategoIncludeDirs());

        // Determine libspoofax2 definition directories.
        final HashSet<HierarchicalResource> libSpoofax2DefinitionDirs = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        if(input.spoofax3LanguageProject().includeLibSpoofax2Exports()) {
            final ClassLoaderResourceLocations locations = libSpoofax2ClassLoaderResources.definitionDirectory.getLocations();
            libSpoofax2DefinitionDirs.addAll(locations.directories);
            final ResourcePath unarchiveDirectoryBase = input.spoofax3LanguageProject().unarchiveDirectory().appendRelativePath("libspoofax2");
            for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
                final ResourcePath jarFilePath = jarFileWithPath.file.getPath();
                final ResourcePath unarchiveDirectory = unarchiveDirectoryBase.appendRelativePath(jarFilePath.getLeaf());
                final Task<?> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, false, false));
                originTasks.add(task.toSupplier());
                context.require(task);
                libSpoofax2DefinitionDirs.add(context.getHierarchicalResource(unarchiveDirectory.appendAsRelativePath(jarFileWithPath.path)));
            }
        }
        for(String export : LibSpoofax2Exports.getStrategoExports()) {
            for(HierarchicalResource definitionDir : libSpoofax2DefinitionDirs) {
                final HierarchicalResource exportDirectory = definitionDir.appendAsRelativePath(export);
                if(exportDirectory.exists()) {
                    includeDirs.add(exportDirectory.getPath());
                }
            }
        }

        // Determine libstatix definition directories.
        final HashSet<HierarchicalResource> libStatixDefinitionDirs = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        if(input.spoofax3LanguageProject().includeLibStatixExports()) {
            final ClassLoaderResourceLocations locations = libStatixClassLoaderResources.definitionDirectory.getLocations();
            libStatixDefinitionDirs.addAll(locations.directories);
            final ResourcePath unarchiveDirectoryBase = input.spoofax3LanguageProject().unarchiveDirectory().appendRelativePath("libstatix");
            for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
                final ResourcePath jarFilePath = jarFileWithPath.file.getPath();
                final ResourcePath unarchiveDirectory = unarchiveDirectoryBase.appendRelativePath(jarFilePath.getLeaf());
                final Task<?> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, false, false));
                originTasks.add(task.toSupplier());
                context.require(task);
                libStatixDefinitionDirs.add(context.getHierarchicalResource(unarchiveDirectory.appendAsRelativePath(jarFileWithPath.path)));
            }
        }
        for(String export : LibStatixExports.getStrategoExports()) {
            for(HierarchicalResource definitionDir : libStatixDefinitionDirs) {
                final HierarchicalResource exportDirectory = definitionDir.appendAsRelativePath(export);
                if(exportDirectory.exists()) {
                    includeDirs.add(exportDirectory.getPath());
                }
            }
        }

        // Set analyze configuration.
        final ListView<ResourcePath> finalIncludeDirs = ListView.copyOf(includeDirs);
        final ListView<String> finalBuiltinLibs = ListView.copyOf(input.strategoBuiltinLibs());
        final StrategoAnalyzeConfig analyzeConfig = new StrategoAnalyzeConfig(
            input.strategoRootDirectory(),
            input.strategoMainFile(),
            finalIncludeDirs,
            finalBuiltinLibs
        );
        configurator.setAnalyzeConfig(input.strategoRootDirectory(), analyzeConfig);

        // Check Stratego source files.
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            new StrategoCheckMulti.Input(rootDirectory.getPath(), StrategoUtil.createResourceWalker(), StrategoUtil.createResourceMatcher(), originTasks)
        ));
        if(messages.containsError()) {
            return Result.ofErr(StrategoCompilerException.checkFail(messages));
        }

        // Compile Stratego sources to Java sources.
        final StrategoCompileConfig compileConfig = new StrategoCompileConfig(
            input.strategoRootDirectory(),
            input.strategoMainFile(),
            finalIncludeDirs,
            finalBuiltinLibs,
            null, //input.strategoCacheDir().orElse(null), // TODO: this makes the compiler crash
            input.strategoOutputDir(),
            input.strategoOutputJavaPackageId()
        );
        final StrategoCompileToJava.Input compileInput = new StrategoCompileToJava.Input(
            compileConfig,
            originTasks
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

        Spoofax3LanguageProject spoofax3LanguageProject();


        default void syncTo(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
            builder.addStrategyPackageIds(strategoOutputJavaPackageId());
            builder.addInteropRegisterersByReflection(strategoOutputJavaPackageId() + ".InteropRegisterer");
        }
    }

    public static class Args implements Serializable {
        public final Input input;
        public final ArrayList<STask<?>> originTasks;

        public Args(Input input, ArrayList<STask<?>> originTasks) {
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
