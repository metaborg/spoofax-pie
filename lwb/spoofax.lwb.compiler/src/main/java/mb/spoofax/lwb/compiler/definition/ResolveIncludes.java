package mb.spoofax.lwb.compiler.definition;

import mb.cfg.Dependency;
import mb.cfg.DependencyKind;
import mb.cfg.DependencySource;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.SerializableFunction;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.component.Component;
import mb.spoofax.core.language.Export;
import mb.spoofax.core.language.ResourceExports;
import mb.spoofax.core.resource.ResourcesComponent;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerComponentManagerWrapper;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Provider;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

public abstract class ResolveIncludes implements TaskDef<ResolveIncludes.Input, Result<ListView<ResourcePath>, ResolveIncludesException>> {
    public static class Input implements Serializable {
        public final ResourcePath rootDirectory;
        public final ResourcePath unarchiveDirectoryBase;

        public Input(
            ResourcePath rootDirectory,
            ResourcePath unarchiveDirectoryBase
        ) {
            this.rootDirectory = rootDirectory;
            this.unarchiveDirectoryBase = unarchiveDirectoryBase;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!rootDirectory.equals(input.rootDirectory)) return false;
            return unarchiveDirectoryBase.equals(input.unarchiveDirectoryBase);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + unarchiveDirectoryBase.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Input{" +
                "rootDirectory=" + rootDirectory +
                ", unarchiveDirectoryBase=" + unarchiveDirectoryBase +
                '}';
        }
    }

    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;
    private final LanguageDefinitionManager languageDefinitionManager;
    private final SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper;
    private final UnarchiveFromJar unarchiveFromJar;
    private final PathStringMatcher unarchiveMatcher;
    private final String exportsId;
    private final Provider<? extends TaskDef<ResourcePath, ? extends Result<?, ?>>> configureTaskDefProvider;
    private final SerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<ListView<String>>, CfgRootDirectoryToObjectException>> exportsFunction;
    private final String metaLanguageName;

    public ResolveIncludes(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        LanguageDefinitionManager languageDefinitionManager,
        SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper,
        UnarchiveFromJar unarchiveFromJar,
        PathStringMatcher unarchiveMatcher,
        String exportsId,
        Provider<? extends TaskDef<ResourcePath, ? extends Result<?, ?>>> configureTaskDefProvider,
        SerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<ListView<String>>, CfgRootDirectoryToObjectException>> exportsFunction,
        String metaLanguageName
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.languageDefinitionManager = languageDefinitionManager;
        this.componentManagerWrapper = componentManagerWrapper;
        this.unarchiveFromJar = unarchiveFromJar;
        this.unarchiveMatcher = unarchiveMatcher;
        this.exportsId = exportsId;
        this.configureTaskDefProvider = configureTaskDefProvider;
        this.exportsFunction = exportsFunction;
        this.metaLanguageName = metaLanguageName;
    }

    @Override
    public Result<ListView<ResourcePath>, ResolveIncludesException> exec(ExecContext context, Input input) throws Exception {
        // TODO: need a dependency to the component manager and language definition manager; what if a language
        //       definition is added/removed, or if a language component is added/removed? That could invalidate the
        //       resolved includes!
        final ResourcePath rootDirectory = input.rootDirectory;
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, DependenciesMapping.instance)
            .mapErr(e -> ResolveIncludesException.getConfigurationFail(rootDirectory, e))
            .flatMap(dependencies -> resolve(context, input, dependencies));
    }

    private Result<ListView<ResourcePath>, ResolveIncludesException> resolve(ExecContext context, Input input, ListView<Dependency> dependencies) {
        final ArrayList<ResourcePath> includes = new ArrayList<>();
        for(Dependency dependency : dependencies) {
            if(!dependency.kinds.contains(DependencyKind.CompileTime)) continue;
            final Result<ListView<ResourcePath>, ResolveIncludesException> result = resolve(context, input, dependency.source);
            if(result.isErr()) {
                return result.ignoreValueIfErr();
            } else {
                // noinspection ConstantConditions (value is present because !result.isErr())
                result.get().addAllTo(includes);
            }
        }
        return Result.ofOk(ListView.of(includes));
    }

    private Result<ListView<ResourcePath>, ResolveIncludesException> resolve(
        ExecContext context,
        Input input,
        DependencySource source
    ) {
        return source.caseOf()
            .coordinateRequirement(coordinateRequirement -> resolve(context, source, input.unarchiveDirectoryBase, coordinateRequirement))
            .coordinate(coordinate -> resolve(context, source, input.unarchiveDirectoryBase, coordinate))
            .path(path -> Result.ofOkOrCatching(() -> resolveFromLanguageDefinition(context, source, input.rootDirectory, path), ResolveIncludesException.class));
    }

    private Result<ListView<ResourcePath>, ResolveIncludesException> resolve(
        ExecContext context,
        DependencySource source,
        ResourcePath unarchiveDirectoryBase,
        Coordinate coordinate
    ) {
        final Class<ResolveIncludesException> exceptionClass = ResolveIncludesException.class;
        return languageDefinitionManager.getLanguageDefinition(coordinate)
            .mapCatching(rootDirectory -> resolveFromLanguageDefinition(context, source, rootDirectory), exceptionClass)
            .orElse(() -> componentManagerWrapper.get().getComponent(coordinate).mapCatching(component -> resolveFromComponent(context, source, unarchiveDirectoryBase, component), exceptionClass))
            .unwrapOrElse(() -> Result.ofErr(ResolveIncludesException.languageDefinitionOrComponentNotFoundFail(source, coordinate)))
            ;
    }

    private Result<ListView<ResourcePath>, ResolveIncludesException> resolve(
        ExecContext context,
        DependencySource source,
        ResourcePath unarchiveDirectoryBase,
        CoordinateRequirement coordinateRequirement
    ) {
        final Class<ResolveIncludesException> exceptionClass = ResolveIncludesException.class;
        return languageDefinitionManager.getOneLanguageDefinition(coordinateRequirement)
            .mapCatching(rootDirectory -> resolveFromLanguageDefinition(context, source, rootDirectory), exceptionClass)
            .orElse(() -> componentManagerWrapper.get().getOneComponent(coordinateRequirement).mapCatching(component -> resolveFromComponent(context, source, unarchiveDirectoryBase, component), exceptionClass))
            .unwrapOrElse(() -> Result.ofErr(ResolveIncludesException.languageDefinitionOrComponentNotFoundOrMultipleFail(source, coordinateRequirement)))
            ;
    }


    private ListView<ResourcePath> resolveFromComponent(
        ExecContext context,
        DependencySource source,
        ResourcePath unarchiveDirectoryBase,
        Component component
    ) throws ResolveIncludesException {
        final Coordinate coordinate = component.getCoordinate();
        final ResourceExports resourceExports = component.getLanguageComponent().map(lc -> lc.getLanguageInstance().getResourceExports())
            .unwrapOrElseThrow(() -> ResolveIncludesException.noResourcesComponentFail(source, coordinate));
        final ClassLoaderResource definitionDirectory = component.getResourcesComponent().map(ResourcesComponent::getDefinitionDirectory)
            .unwrapOrElseThrow(() -> ResolveIncludesException.noLanguageComponentFail(source, coordinate));
        final HashSet<ResourcePath> definitionLocations = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        try {
            final ClassLoaderResourceLocations<FSResource> locations = definitionDirectory.getLocations();
            for(FSResource directory : locations.directories) {
                definitionLocations.add(directory.getPath());
            }
            for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
                final ResourcePath jarFilePath = jarFileWithPath.file.getPath();
                @SuppressWarnings("ConstantConditions") // JAR files always have leaves.
                final ResourcePath unarchiveDirectory = unarchiveDirectoryBase.appendRelativePath(jarFilePath.getLeaf());
                final Task<?> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, unarchiveMatcher, false, false));
                context.require(task); // HACK: eagerly unarchive such that the directory and contents exist.
                definitionLocations.add(unarchiveDirectory.appendAsRelativePath(jarFileWithPath.path));
            }
        } catch(IOException e) {
            throw ResolveIncludesException.getClassLoaderResourcesLocationsFail(source, coordinate, e);
        }

        final ArrayList<ResourcePath> includes = new ArrayList<>();
        try {
            for(Export export : resourceExports.getExports(exportsId)) {
                for(ResourcePath definitionLocation : definitionLocations) {
                    final ResourcePath include = definitionLocation.appendAsRelativePath(export.getRelativePath());
                    includes.add(include);
                }
            }
        } catch(IllegalArgumentException e) {
            throw ResolveIncludesException.noResourceExportsFail(source, coordinate, metaLanguageName);
        }

        return ListView.of(includes);
    }

    private ListView<ResourcePath> resolveFromLanguageDefinition(
        ExecContext context,
        DependencySource source,
        ResourcePath dependencySourceContext,
        String path
    ) throws ResolveIncludesException {
        return resolveFromLanguageDefinition(context, source, dependencySourceContext.appendOrReplaceWithPath(path));
    }

    private ListView<ResourcePath> resolveFromLanguageDefinition(
        ExecContext context,
        DependencySource source,
        ResourcePath rootDirectory
    ) throws ResolveIncludesException {
        // Require Stratego configure so that this task depends on all tasks that generate Stratego code.
        context.require(configureTaskDefProvider.get().createTask(rootDirectory), OutputStampers.inconsequential())
            .mapErr(e -> ResolveIncludesException.configureFail(source, rootDirectory, metaLanguageName, e))
            .throwIfError();
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, exportsFunction)
            .mapErr(e -> ResolveIncludesException.getConfigurationFail(rootDirectory, e))
            .mapThrowing(o -> o.mapOrElseThrow(
                exports -> ListView.copyOf(exports.stream().map(rootDirectory::appendAsRelativePath)),
                () -> ResolveIncludesException.noConfigurationFail(source, rootDirectory, metaLanguageName)
            ))
            .unwrap();
    }
}
