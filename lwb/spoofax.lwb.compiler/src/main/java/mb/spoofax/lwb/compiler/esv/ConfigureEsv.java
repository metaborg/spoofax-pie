package mb.spoofax.lwb.compiler.esv;

import mb.cfg.CompileLanguageInput;
import mb.cfg.CompileLanguageShared;
import mb.cfg.CompileLanguageToJavaClassPathInput;
import mb.cfg.metalang.CompileEsvInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.StreamIterable;
import mb.esv.task.EsvConfig;
import mb.libspoofax2.LibSpoofax2ClassLoaderResources;
import mb.libspoofax2.LibSpoofax2Exports;
import mb.pie.api.ExecContext;
import mb.pie.api.SerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.ValueSupplier;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToCompletionColorer;
import mb.sdf3.task.util.Sdf3Util;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

public class ConfigureEsv implements TaskDef<ResourcePath, Result<Option<EsvConfig>, EsvConfigureException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final UnarchiveFromJar unarchiveFromJar;
    private final LibSpoofax2ClassLoaderResources libSpoofax2ClassLoaderResources;

    private final Sdf3Parse sdf3Parse;
    private final Sdf3Desugar sdf3Desugar;
    private final Sdf3ToCompletionColorer sdf3ToCompletionColorer;


    @Inject public ConfigureEsv(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,

        UnarchiveFromJar unarchiveFromJar,
        LibSpoofax2ClassLoaderResources libSpoofax2ClassLoaderResources,

        Sdf3Parse sdf3Parse,
        Sdf3Desugar sdf3Desugar,
        Sdf3ToCompletionColorer sdf3ToCompletionColorer
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;

        this.unarchiveFromJar = unarchiveFromJar;
        this.libSpoofax2ClassLoaderResources = libSpoofax2ClassLoaderResources;

        this.sdf3Parse = sdf3Parse;
        this.sdf3Desugar = sdf3Desugar;
        this.sdf3ToCompletionColorer = sdf3ToCompletionColorer;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<EsvConfig>, EsvConfigureException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(EsvConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<EsvConfig>, IOException>flatMapThrowing(cfgOutput -> Result.transpose(Option.ofOptional(cfgOutput.compileLanguageToJavaClassPathInput.compileLanguageInput().esv())
                .mapThrowing(esvInput -> toEsvConfig(context, cfgOutput.compileLanguageToJavaClassPathInput, esvInput))
            ));
    }


    public Result<EsvConfig, EsvConfigureException> toEsvConfig(ExecContext context, CompileLanguageToJavaClassPathInput input, CompileEsvInput esvInput) throws IOException {
        final CompileLanguageInput compileLanguageInput = input.compileLanguageInput();
        final CompileLanguageShared compileLanguageShared = compileLanguageInput.compileLanguageShared();

        // Check main source directory, main file, and include directories.
        final HierarchicalResource mainSourceDirectory = context.require(esvInput.mainSourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(EsvConfigureException.mainSourceDirectoryFail(mainSourceDirectory.getPath()));
        }
        final HierarchicalResource mainFile = context.require(esvInput.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(EsvConfigureException.mainFileFail(mainFile.getPath()));
        }
        for(ResourcePath includeDirectoryPath : esvInput.includeDirectories()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(EsvConfigureException.includeDirectoryFail(includeDirectoryPath));
            }
        }

        // Unarchive ESV files from libspoofax2.
        final LinkedHashSet<HierarchicalResource> libSpoofax2DefinitionDirs = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        final LinkedHashSet<Supplier<ResourcePath>> libSpoofax2UnarchiveDirSuppliers = new LinkedHashSet<>();
        if(compileLanguageShared.includeLibSpoofax2Exports()) {
            final ClassLoaderResourceLocations locations = libSpoofax2ClassLoaderResources.definitionDirectory.getLocations();
            libSpoofax2DefinitionDirs.addAll(locations.directories);
            final ResourcePath libSpoofax2UnarchiveDirectory = compileLanguageShared.unarchiveDirectory().appendRelativePath("libspoofax2");
            for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
                final ResourcePath jarFilePath = jarFileWithPath.file.getPath();
                @SuppressWarnings("ConstantConditions") // JAR files always have leaves.
                final ResourcePath unarchiveDirectory = libSpoofax2UnarchiveDirectory.appendRelativePath(jarFilePath.getLeaf());
                libSpoofax2UnarchiveDirSuppliers.add(unarchiveFromJar
                    .createSupplier(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, PathStringMatcher.ofExtension("esv"), false, false))
                    .map(new AppendPath(jarFileWithPath.path))
                );
            }
        }

        // Gather include directories.
        final LinkedHashSet<Supplier<Result<ResourcePath, ?>>> includeDirectorySuppliers = new LinkedHashSet<>();
        // Add main source directory as an include for resolving imports.
        includeDirectorySuppliers.add(new ValueSupplier<>(Result.ofOk(esvInput.mainSourceDirectory())));
        for(ResourcePath includeDirectory : esvInput.includeDirectories()) {
            includeDirectorySuppliers.add(new ValueSupplier<>(Result.ofOk(includeDirectory)));
        }
        for(String export : LibSpoofax2Exports.getEsvExports()) {
            for(HierarchicalResource definitionDir : libSpoofax2DefinitionDirs) {
                final HierarchicalResource exportDirectory = definitionDir.appendAsRelativePath(export);
                if(exportDirectory.exists()) {
                    includeDirectorySuppliers.add(new ValueSupplier<>(Result.ofOk(exportDirectory.getPath())));
                }
            }
            for(Supplier<ResourcePath> unarchiveDirSupplier : libSpoofax2UnarchiveDirSuppliers) {
                includeDirectorySuppliers.add(unarchiveDirSupplier.map(new AppendPath(export)).map(MakeOk.instance));
            }
        }

        // SDF3 to completion colorer (if SDF3 is enabled).
        final ArrayList<Supplier<Result<IStrategoTerm, ?>>> includeAstSuppliers = new ArrayList<>();
        Option.ofOptional(compileLanguageInput.sdf3()).ifSomeThrowing(sdf3Input -> {
            final HierarchicalResource sdf3SourceDirectory = context.getHierarchicalResource(sdf3Input.sourceDirectory());
            final ResourceWalker resourceWalker = Sdf3Util.createResourceWalker();
            final ResourceMatcher resourceMatcher = new AllResourceMatcher(Sdf3Util.createResourceMatcher(), new FileResourceMatcher());
            context.require(sdf3SourceDirectory, ResourceStampers.modifiedDirRec(resourceWalker, resourceMatcher));
            try(final Stream<? extends HierarchicalResource> stream = sdf3SourceDirectory.walk(resourceWalker, resourceMatcher)) {
                for(HierarchicalResource sdf3File : new StreamIterable<>(stream)) {
                    final Supplier<Result<IStrategoTerm, ?>> astSupplier = sdf3Desugar.createSupplier(sdf3Parse.createAstSupplier(sdf3File.getPath()));
                    includeAstSuppliers.add(sdf3ToCompletionColorer.createSupplier(astSupplier));
                }
            }
        });

        return Result.ofOk(new EsvConfig(esvInput.mainFile(), ListView.of(), ListView.copyOf(includeDirectorySuppliers), ListView.of(includeAstSuppliers)));
    }


    private static class AppendPath implements SerializableFunction<ResourcePath, ResourcePath> {
        private final String path;

        private AppendPath(String path) {
            this.path = path;
        }

        @Override public ResourcePath apply(ResourcePath dir) {
            return dir.appendAsRelativePath(path);
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final AppendPath that = (AppendPath)o;
            return path.equals(that.path);
        }

        @Override public int hashCode() {
            return path.hashCode();
        }

        @Override public String toString() {
            return "AppendPath{" +
                "path='" + path + '\'' +
                '}';
        }
    }

    private static class MakeOk implements SerializableFunction<ResourcePath, Result<ResourcePath, ?>> {
        private static final MakeOk instance = new MakeOk();

        private MakeOk() {}

        @Override public Result<ResourcePath, ?> apply(ResourcePath dir) {
            return Result.ofOk(dir);
        }

        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        @Override public String toString() { return "MakeOk()"; }

        private Object readResolve() { return instance; }
    }
}
