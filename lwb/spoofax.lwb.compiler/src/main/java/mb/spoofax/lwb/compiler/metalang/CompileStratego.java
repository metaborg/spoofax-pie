package mb.spoofax.lwb.compiler.metalang;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ADT;
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
import mb.resource.ResourceKey;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileStrategoInput;
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
import java.util.Objects;
import java.util.Optional;

@Value.Enclosing
public class CompileStratego implements TaskDef<CompileStratego.Args, Result<KeyedMessages, CompileStratego.StrategoCompileException>> {
    public static class Args implements Serializable {
        public final CompileStrategoInput input;
        public final ArrayList<STask<?>> originTasks;

        public Args(CompileStrategoInput input, ArrayList<STask<?>> originTasks) {
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


    private final StrategoCheckMulti check;
    private final StrategoCompileToJava compileToJava;
    private final StrategoConfigurator configurator;

    private final UnarchiveFromJar unarchiveFromJar;
    private final LibSpoofax2ClassLoaderResources libSpoofax2ClassLoaderResources;
    private final LibStatixClassLoaderResources libStatixClassLoaderResources;

    @Inject public CompileStratego(
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
    public Result<KeyedMessages, StrategoCompileException> exec(ExecContext context, Args args) throws Exception {
        final CompileStrategoInput input = args.input;

        // Check main file, root directory, and include directories.
        final HierarchicalResource mainFile = context.require(input.strategoMainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(StrategoCompileException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource rootDirectory = context.require(input.strategoRootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(StrategoCompileException.rootDirectoryFail(rootDirectory.getPath()));
        }
        for(ResourcePath includeDirectoryPath : input.strategoIncludeDirs()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(StrategoCompileException.includeDirectoryFail(includeDirectoryPath));
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
        if(input.compileLanguageShared().includeLibSpoofax2Exports()) {
            final ClassLoaderResourceLocations locations = libSpoofax2ClassLoaderResources.definitionDirectory.getLocations();
            libSpoofax2DefinitionDirs.addAll(locations.directories);
            final ResourcePath unarchiveDirectoryBase = input.compileLanguageShared().unarchiveDirectory().appendRelativePath("libspoofax2");
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
        if(input.compileLanguageShared().includeLibStatixExports()) {
            final ClassLoaderResourceLocations locations = libStatixClassLoaderResources.definitionDirectory.getLocations();
            libStatixDefinitionDirs.addAll(locations.directories);
            final ResourcePath unarchiveDirectoryBase = input.compileLanguageShared().unarchiveDirectory().appendRelativePath("libstatix");
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
            return Result.ofErr(StrategoCompileException.checkFail(messages));
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
            return Result.ofErr(StrategoCompileException.compileFail(compileResult.getErr()));
        }

        return Result.ofOk(messages);
    }


    @ADT
    public abstract static class StrategoCompileException extends Exception implements HasOptionalMessages {
        public interface Cases<R> {
            R mainFileFail(ResourceKey mainFile);

            R includeDirectoryFail(ResourcePath includeDirectory);

            R rootDirectoryFail(ResourcePath rootDirectory);

            R checkFail(KeyedMessages messages);

            R compileFail(Exception cause);
        }

        public static StrategoCompileException mainFileFail(ResourceKey mainFile) {
            return StrategoCompileExceptions.mainFileFail(mainFile);
        }

        public static StrategoCompileException includeDirectoryFail(ResourcePath includeDirectory) {
            return StrategoCompileExceptions.includeDirectoryFail(includeDirectory);
        }

        public static StrategoCompileException rootDirectoryFail(ResourcePath rootDirectory) {
            return StrategoCompileExceptions.rootDirectoryFail(rootDirectory);
        }

        public static StrategoCompileException checkFail(KeyedMessages messages) {
            return StrategoCompileExceptions.checkFail(messages);
        }

        public static StrategoCompileException compileFail(Exception cause) {
            return withCause(StrategoCompileExceptions.compileFail(cause), cause);
        }

        private static StrategoCompileException withCause(StrategoCompileException e, Exception cause) {
            e.initCause(cause);
            return e;
        }


        public abstract <R> R match(Cases<R> cases);

        public StrategoCompileExceptions.CasesMatchers.TotalMatcher_MainFileFail cases() {
            return StrategoCompileExceptions.cases();
        }

        public StrategoCompileExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
            return StrategoCompileExceptions.caseOf(this);
        }


        @Override public String getMessage() {
            return caseOf()
                .mainFileFail((mainFile) -> "Stratego main file '" + mainFile + "' does not exist or is not a file")
                .includeDirectoryFail((includeDirectory) -> "Stratego include directory '" + includeDirectory + "' does not exist or is not a directory")
                .rootDirectoryFail((rootDirectory) -> "Stratego root directory '" + rootDirectory + "' does not exist or is not a directory")
                .checkFail((messages) -> "Parsing or checking Stratego source files failed")
                .compileFail((cause) -> "Stratego compiler failed unexpectedly")
                ;
        }

        @Override public Throwable fillInStackTrace() {
            return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
        }

        @Override public Optional<KeyedMessages> getOptionalMessages() {
            return StrategoCompileExceptions.getMessages(this);
        }


        @Override public abstract int hashCode();

        @Override public abstract boolean equals(@Nullable Object obj);

        @Override public abstract String toString();
    }
}
