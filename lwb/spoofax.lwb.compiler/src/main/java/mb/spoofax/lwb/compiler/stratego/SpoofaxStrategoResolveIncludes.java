package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.Dependency;
import mb.cfg.DependencyKind;
import mb.cfg.DependencySource;
import mb.cfg.metalang.CfgStrategoConfig;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
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
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerScope;
import mb.spoofax.lwb.compiler.definition.DependenciesMapping;
import mb.spoofax.lwb.compiler.definition.LanguageDefinitionManager;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

@SpoofaxLwbCompilerScope
public class SpoofaxStrategoResolveIncludes implements TaskDef<SpoofaxStrategoResolveIncludes.Input, Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException>> {
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
    private final Provider<SpoofaxStrategoConfigure> strategoConfigure;

    @Inject public SpoofaxStrategoResolveIncludes(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        LanguageDefinitionManager languageDefinitionManager,
        SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper,
        UnarchiveFromJar unarchiveFromJar,
        Provider<SpoofaxStrategoConfigure> strategoConfigure
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.languageDefinitionManager = languageDefinitionManager;
        this.componentManagerWrapper = componentManagerWrapper;
        this.unarchiveFromJar = unarchiveFromJar;
        this.strategoConfigure = strategoConfigure;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException> exec(ExecContext context, Input input) throws Exception {
        // TODO: need a dependency to the component manager and language definition manager; what if a language
        //       definition is added/removed, or if a language component is added/removed? That could invalidate the
        //       resolved includes!
        final ResourcePath rootDirectory = input.rootDirectory;
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, DependenciesMapping.instance)
            .mapErr(e -> SpoofaxStrategoResolveIncludesException.getConfigurationFail(rootDirectory, e))
            .flatMap(dependencies -> resolve(context, input, dependencies));
    }

    private Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException> resolve(ExecContext context, Input input, ListView<Dependency> dependencies) {
        final ArrayList<ResourcePath> includes = new ArrayList<>();
        for(Dependency dependency : dependencies) {
            if(!dependency.kinds.contains(DependencyKind.CompileTime)) continue;
            final Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException> result = resolve(context, input, dependency.source);
            if(result.isErr()) {
                return result.ignoreValueIfErr();
            } else {
                // noinspection ConstantConditions (value is present because !result.isErr())
                result.get().addAllTo(includes);
            }
        }
        return Result.ofOk(ListView.of(includes));
    }

    private Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException> resolve(
        ExecContext context,
        Input input,
        DependencySource source
    ) {
        return source.caseOf()
            .coordinateRequirement(coordinateRequirement -> resolve(context, source, input.unarchiveDirectoryBase, coordinateRequirement))
            .coordinate(coordinate -> resolve(context, source, input.unarchiveDirectoryBase, coordinate))
            .path(path -> Result.ofOkOrCatching(() -> resolveFromLanguageDefinition(context, source, input.rootDirectory, path), SpoofaxStrategoResolveIncludesException.class));
    }

    private Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException> resolve(
        ExecContext context,
        DependencySource source,
        ResourcePath unarchiveDirectoryBase,
        Coordinate coordinate
    ) {
        final Class<SpoofaxStrategoResolveIncludesException> exceptionClass = SpoofaxStrategoResolveIncludesException.class;
        return languageDefinitionManager.getLanguageDefinition(coordinate)
            .mapCatching(rootDirectory -> resolveFromLanguageDefinition(context, source, rootDirectory), exceptionClass)
            .orElse(() -> componentManagerWrapper.get().getComponent(coordinate).mapCatching(component -> resolveFromComponent(context, source, unarchiveDirectoryBase, component), exceptionClass))
            .unwrapOrElse(() -> Result.ofErr(SpoofaxStrategoResolveIncludesException.languageDefinitionOrComponentNotFoundFail(source, coordinate)))
            ;
    }

    private Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException> resolve(
        ExecContext context,
        DependencySource source,
        ResourcePath unarchiveDirectoryBase,
        CoordinateRequirement coordinateRequirement
    ) {
        final Class<SpoofaxStrategoResolveIncludesException> exceptionClass = SpoofaxStrategoResolveIncludesException.class;
        return languageDefinitionManager.getOneLanguageDefinition(coordinateRequirement)
            .mapCatching(rootDirectory -> resolveFromLanguageDefinition(context, source, rootDirectory), exceptionClass)
            .orElse(() -> componentManagerWrapper.get().getOneComponent(coordinateRequirement).mapCatching(component -> resolveFromComponent(context, source, unarchiveDirectoryBase, component), exceptionClass))
            .unwrapOrElse(() -> Result.ofErr(SpoofaxStrategoResolveIncludesException.languageDefinitionOrComponentNotFoundOrMultipleFail(source, coordinateRequirement)))
            ;
    }


    private ListView<ResourcePath> resolveFromComponent(
        ExecContext context,
        DependencySource source,
        ResourcePath unarchiveDirectoryBase,
        Component component
    ) throws SpoofaxStrategoResolveIncludesException {
        final Coordinate coordinate = component.getCoordinate();
        final ResourceExports resourceExports = component.getLanguageComponent().map(lc -> lc.getLanguageInstance().getResourceExports())
            .unwrapOrElseThrow(() -> SpoofaxStrategoResolveIncludesException.noResourcesComponentFail(source, coordinate));
        final ClassLoaderResource definitionDirectory = component.getResourcesComponent().map(ResourcesComponent::getDefinitionDirectory)
            .unwrapOrElseThrow(() -> SpoofaxStrategoResolveIncludesException.noLanguageComponentFail(source, coordinate));
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
                final Task<?> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, PathStringMatcher.ofExtensions("str2", "str"), false, false));
                context.require(task); // HACK: eagerly unarchive such that the directory and contents exist.
                definitionLocations.add(unarchiveDirectory.appendAsRelativePath(jarFileWithPath.path));
            }
        } catch(IOException e) {
            throw SpoofaxStrategoResolveIncludesException.getClassLoaderResourcesLocationsFail(source, coordinate, e);
        }

        final ArrayList<ResourcePath> includes = new ArrayList<>();
        try {
            for(Export export : resourceExports.getExports("Stratego")) {
                for(ResourcePath definitionLocation : definitionLocations) {
                    final ResourcePath include = definitionLocation.appendAsRelativePath(export.getRelativePath());
                    includes.add(include);
                }
            }
        } catch(IllegalArgumentException e) {
            throw SpoofaxStrategoResolveIncludesException.noStrategoResourceExportsFail(source, coordinate);
        }

        return ListView.of(includes);
    }

    private ListView<ResourcePath> resolveFromLanguageDefinition(
        ExecContext context,
        DependencySource source,
        ResourcePath dependencySourceContext,
        String path
    ) throws SpoofaxStrategoResolveIncludesException {
        return resolveFromLanguageDefinition(context, source, dependencySourceContext.appendOrReplaceWithPath(path));
    }

    private ListView<ResourcePath> resolveFromLanguageDefinition(
        ExecContext context,
        DependencySource source,
        ResourcePath rootDirectory
    ) throws SpoofaxStrategoResolveIncludesException {
        // Require Stratego configure so that this task depends on all tasks that generate Stratego code.
        context.require(strategoConfigure.get().createTask(rootDirectory), OutputStampers.inconsequential())
            .mapErr(e -> SpoofaxStrategoResolveIncludesException.strategoConfigureFail(source, rootDirectory, e))
            .throwIfError();
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, SpoofaxStrategoConfigMapper.instance)
            .mapErr(e -> SpoofaxStrategoResolveIncludesException.getConfigurationFail(rootDirectory, e))
            .mapThrowing(o -> o.mapOrElseThrow(
                c -> resolveFromLanguageDefinition(rootDirectory, c),
                () -> SpoofaxStrategoResolveIncludesException.noStrategoConfiguration(source, rootDirectory)
            ))
            .unwrap();
    }

    private ListView<ResourcePath> resolveFromLanguageDefinition(
        ResourcePath rootDirectory,
        CfgStrategoConfig config
    ) {
        return ListView.copyOf(config.source().getFiles().exportDirectories().stream()
            .map(rootDirectory::appendAsRelativePath));
    }
}
