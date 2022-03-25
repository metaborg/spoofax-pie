package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.DependencySource;
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
        public final DependencySource dependencySource;
        public final ResourcePath dependencySourceContext;
        public final ResourcePath unarchiveDirectoryBase;

        public Input(
            DependencySource dependencySource,
            ResourcePath dependencySourceContext,
            ResourcePath unarchiveDirectoryBase
        ) {
            this.dependencySource = dependencySource;
            this.dependencySourceContext = dependencySourceContext;
            this.unarchiveDirectoryBase = unarchiveDirectoryBase;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!dependencySource.equals(input.dependencySource)) return false;
            if(!dependencySourceContext.equals(input.dependencySourceContext)) return false;
            return unarchiveDirectoryBase.equals(input.unarchiveDirectoryBase);
        }

        @Override public int hashCode() {
            int result = dependencySource.hashCode();
            result = 31 * result + dependencySourceContext.hashCode();
            result = 31 * result + unarchiveDirectoryBase.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Input{" +
                "dependencySource=" + dependencySource +
                ", dependencySourceContext=" + dependencySourceContext +
                ", unarchiveDirectoryBase=" + unarchiveDirectoryBase +
                '}';
        }
    }

    private final LanguageDefinitionManager languageDefinitionManager;
    private final SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper;
    private final UnarchiveFromJar unarchiveFromJar;
    private final Provider<SpoofaxStrategoConfigure> strategoConfigure;
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    @Inject public SpoofaxStrategoResolveIncludes(
        LanguageDefinitionManager languageDefinitionManager,
        SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper,
        UnarchiveFromJar unarchiveFromJar,
        Provider<SpoofaxStrategoConfigure> strategoConfigure,
        CfgRootDirectoryToObject cfgRootDirectoryToObject
    ) {
        this.languageDefinitionManager = languageDefinitionManager;
        this.componentManagerWrapper = componentManagerWrapper;
        this.unarchiveFromJar = unarchiveFromJar;
        this.strategoConfigure = strategoConfigure;
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException> exec(ExecContext context, Input input) throws Exception {
        // TODO: need a dependency to the component manager and language definition manager; what if a language
        //       definition is added/removed, or if a language component is added/removed? That could invalidate the
        //       resolved includes!
        return input.dependencySource.caseOf()
            .coordinateRequirement(coordinateRequirement -> resolve(context, input, coordinateRequirement))
            .coordinate(coordinate -> resolve(context, input, coordinate))
            .path(path -> Result.ofOkOrCatching(() -> resolveFromLanguageDefinition(context, input, input.dependencySourceContext, path), SpoofaxStrategoResolveIncludesException.class));
    }


    private Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException> resolve(ExecContext context, Input input, Coordinate coordinate) {
        final Class<SpoofaxStrategoResolveIncludesException> exceptionClass = SpoofaxStrategoResolveIncludesException.class;
        return languageDefinitionManager.getLanguageDefinition(coordinate)
            .mapCatching(rootDirectory -> resolveFromLanguageDefinition(context, input, rootDirectory), exceptionClass)
            .orElse(() -> componentManagerWrapper.get().getComponent(coordinate).mapCatching(component -> resolveFromComponent(context, input, component), exceptionClass))
            .unwrapOrElse(() -> Result.ofErr(SpoofaxStrategoResolveIncludesException.languageDefinitionOrComponentNotFoundFail(input.dependencySource, coordinate)))
            ;
    }

    private Result<ListView<ResourcePath>, SpoofaxStrategoResolveIncludesException> resolve(ExecContext context, Input input, CoordinateRequirement coordinateRequirement) {
        final Class<SpoofaxStrategoResolveIncludesException> exceptionClass = SpoofaxStrategoResolveIncludesException.class;
        return languageDefinitionManager.getOneLanguageDefinition(coordinateRequirement)
            .mapCatching(rootDirectory -> resolveFromLanguageDefinition(context, input, rootDirectory), exceptionClass)
            .orElse(() -> componentManagerWrapper.get().getOneComponent(coordinateRequirement).mapCatching(component -> resolveFromComponent(context, input, component), exceptionClass))
            .unwrapOrElse(() -> Result.ofErr(SpoofaxStrategoResolveIncludesException.languageDefinitionOrComponentNotFoundOrMultipleFail(input.dependencySource, coordinateRequirement)))
            ;
    }


    private ListView<ResourcePath> resolveFromComponent(ExecContext context, Input input, Component component) throws SpoofaxStrategoResolveIncludesException {
        final DependencySource source = input.dependencySource;
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
                final ResourcePath unarchiveDirectory = input.unarchiveDirectoryBase.appendRelativePath(jarFilePath.getLeaf());
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

    private ListView<ResourcePath> resolveFromLanguageDefinition(ExecContext context, Input input, ResourcePath dependencySourceContext, String path) throws SpoofaxStrategoResolveIncludesException {
        return resolveFromLanguageDefinition(context, input, dependencySourceContext.appendOrReplaceWithPath(path));
    }

    private ListView<ResourcePath> resolveFromLanguageDefinition(ExecContext context, Input input, ResourcePath rootDirectory) throws SpoofaxStrategoResolveIncludesException {
        final DependencySource source = input.dependencySource;
        // Require Stratego configure so that this task depends on all tasks that generate Stratego code.
        context.require(strategoConfigure.get().createTask(rootDirectory), OutputStampers.inconsequential())
            .mapErr(e -> SpoofaxStrategoResolveIncludesException.strategoConfigureFail(source, rootDirectory, e))
            .throwIfError();
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, new SpoofaxStrategoConfigMapper())
            .mapErr(e -> SpoofaxStrategoResolveIncludesException.getConfigurationFail(source, rootDirectory, e))
            .mapThrowing(o -> o.mapOrElseThrow(
                c -> resolveFromLanguageDefinition(rootDirectory, c),
                () -> SpoofaxStrategoResolveIncludesException.noStrategoConfiguration(source, rootDirectory)
            ))
            .unwrap();
    }

    private ListView<ResourcePath> resolveFromLanguageDefinition(ResourcePath rootDirectory, SpoofaxStrategoConfig config) {
        return ListView.copyOf(config.cfgStrategoConfig.source().getFiles().exportDirectories().stream()
            .map(rootDirectory::appendAsRelativePath));
    }
}
