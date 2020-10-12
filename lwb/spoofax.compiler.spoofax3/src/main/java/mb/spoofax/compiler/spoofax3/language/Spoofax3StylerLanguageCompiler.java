package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.esv.spoofax.task.EsvCheckMulti;
import mb.esv.spoofax.task.EsvCompile;
import mb.esv.spoofax.task.EsvParse;
import mb.esv.spoofax.util.EsvUtil;
import mb.libspoofax2.LibSpoofax2Exports;
import mb.libspoofax2.spoofax.LibSpoofax2Qualifier;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.ReadableResource;
import mb.resource.WritableResource;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.str.spoofax.config.StrategoConfigurator;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Value.Enclosing
public class Spoofax3StylerLanguageCompiler implements TaskDef<Spoofax3StylerLanguageCompiler.Input, Result<KeyedMessages, StylerCompilerException>> {
    private final EsvParse parse;
    private final EsvCheckMulti check;
    private final EsvCompile compile;

    private final UnarchiveFromJar unarchiveFromJar;
    private final ClassLoaderResource libSpoofax2DefinitionDir;

    @Inject public Spoofax3StylerLanguageCompiler(
        EsvParse parse,
        EsvCheckMulti check,
        EsvCompile compile,
        StrategoConfigurator configurator,
        UnarchiveFromJar unarchiveFromJar,
        @LibSpoofax2Qualifier("definition-dir") ClassLoaderResource libSpoofax2DefinitionDir
    ) {
        this.parse = parse;
        this.check = check;
        this.compile = compile;
        this.unarchiveFromJar = unarchiveFromJar;
        this.libSpoofax2DefinitionDir = libSpoofax2DefinitionDir;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, StylerCompilerException> exec(ExecContext context, Input input) throws Exception {
        // Check main file, root directory, and include directories.
        final HierarchicalResource mainFile = context.require(input.esvMainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(StylerCompilerException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource rootDirectory = context.require(input.esvRootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(StylerCompilerException.rootDirectoryFail(rootDirectory.getPath()));
        }
        for(ResourcePath includeDirectoryPath : input.esvIncludeDirs()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(StylerCompilerException.includeDirectoryFail(includeDirectoryPath));
            }
        }

        // Gather origins for provided ESV files.
        final ArrayList<STask<?>> originTasks = new ArrayList<>();

        // Determine libspoofax2 definition directories.
        final HashSet<HierarchicalResource> libSpoofax2DefinitionDirs = new HashSet<>(); // Set to remove duplicates.
        if(input.spoofax3LanguageProject().includeLibSpoofax2Exports()) {
            final ClassLoaderResourceLocations locations = libSpoofax2DefinitionDir.getLocations();
            libSpoofax2DefinitionDirs.addAll(locations.directories);
            final ResourcePath unarchiveDirectory = input.spoofax3LanguageProject().unarchiveDirectory().appendRelativePath("libspoofax2");
            for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
                final Task<None> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFileWithPath.file.getPath(), unarchiveDirectory, false, false));
                originTasks.add(task.toSupplier());
                context.require(task);
                libSpoofax2DefinitionDirs.add(context.getHierarchicalResource(unarchiveDirectory.appendAsRelativePath(jarFileWithPath.path)));
            }
        }

        // Gather include directories.
        final ArrayList<ResourcePath> includeDirs = new ArrayList<>(input.esvIncludeDirs());
        includeDirs.add(input.esvRootDirectory());
        for(HierarchicalResource definitionDir : libSpoofax2DefinitionDirs) {
            for(HierarchicalResource export : LibSpoofax2Exports.getEsvExports(definitionDir)) {
                if(export.exists()) {
                    includeDirs.add(export.getPath());
                }
            }
        }

        // Check ESV source files.
        // TODO: this does not check ESV files in include directories.
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            new EsvCheckMulti.Input(rootDirectory.getPath(), EsvUtil.createResourceWalker(), EsvUtil.createResourceMatcher())
        ));
        if(messages.containsError()) {
            return Result.ofErr(StylerCompilerException.checkFail(messages));
        }

        // Compile ESV files to aterm format.
        final Result<IStrategoTerm, ?> result = context.require(compile, new EsvCompile.Input(parse.createAstSupplier(input.esvMainFile()), new ImportFunction(includeDirs), ListView.of(originTasks)));
        if(result.isErr()) {
            return Result.ofErr(StylerCompilerException.compilerFail(result.unwrapErr()));
        }
        final IStrategoTerm atermFormat = result.unwrap();
        final WritableResource atermFormatFile = context.getWritableResource(input.esvAtermFormatOutputFile());
        atermFormatFile.writeString(atermFormat.toString());
        context.provide(atermFormatFile);

        return Result.ofOk(messages);
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax3StylerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        @Value.Default default ResourcePath esvRootDirectory() {
            return spoofax3LanguageProject().languageProject().project().srcMainDirectory().appendRelativePath("esv");
        }

        @Value.Default default ResourcePath esvMainFile() {
            return esvRootDirectory().appendRelativePath("main.esv");
        }

        List<ResourcePath> esvIncludeDirs();


        @Value.Default default String esvAtermFormatFileRelativePath() {
            return "editor.esv.af";
        }

        default ResourcePath esvAtermFormatOutputFile() {
            return spoofax3LanguageProject().generatedResourcesDirectory() // Generated resources directory, so that Gradle includes the aterm format file in the JAR file.
                .appendRelativePath(spoofax3LanguageProject().languageProject().packagePath()) // Append package path to make location unique, enabling JAR files to be merged.
                .appendRelativePath(esvAtermFormatFileRelativePath()) // Append the relative path to the aterm format file.
                ;
        }


        /// Automatically provided sub-inputs

        Spoofax3LanguageProject spoofax3LanguageProject();


        default void syncTo(StylerLanguageCompiler.Input.Builder builder) {
            builder.packedEsvRelativePath(esvAtermFormatFileRelativePath());
        }
    }


    private class ImportFunction implements Function<String, Result<IStrategoTerm, ?>> {
        private final ArrayList<ResourcePath> includeDirs;

        public ImportFunction(ArrayList<ResourcePath> includeDirs) {
            this.includeDirs = includeDirs;
        }

        @Override public Result<IStrategoTerm, ?> apply(ExecContext context, String input) {
            for(ResourcePath includeDir : includeDirs) {
                final ResourcePath path = includeDir.appendRelativePath(input).ensureLeafExtension("esv");
                try {
                    final ReadableResource resource = context.require(path, ResourceStampers.<ReadableResource>exists());
                    if(!resource.exists()) continue;
                    return context.require(parse.createAstSupplier(path));
                } catch(IOException e) {
                    return Result.ofErr(e); // TODO: better error
                }
            }
            return Result.ofErr(new Exception("Could not import '" + input + "', no ESV module found with that name in any import directory"));
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final ImportFunction that = (ImportFunction)o;
            return includeDirs.equals(that.includeDirs);
        }

        @Override public int hashCode() {
            return Objects.hash(includeDirs);
        }
    }
}
