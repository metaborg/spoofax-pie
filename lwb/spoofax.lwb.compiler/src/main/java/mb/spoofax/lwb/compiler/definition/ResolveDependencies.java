package mb.spoofax.lwb.compiler.definition;

import mb.cfg.Dependency;
import mb.cfg.DependencyKind;
import mb.cfg.DependencySource;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.function.Function4Throwing1;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.SerializableFunction;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.component.Component;
import mb.spoofax.core.language.NoResourceExportsException;
import mb.spoofax.core.language.ResourceExports;
import mb.spoofax.core.resource.ResourcesComponent;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerComponentManagerWrapper;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Provider;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;

public abstract class ResolveDependencies<T extends Serializable, C extends Serializable, CE extends Exception> implements TaskDef<ResolveDependencies.Input, Result<ListView<T>, ResolveDependenciesException>> {
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
            return "ResolveDependencies$Input{" +
                "rootDirectory=" + rootDirectory +
                ", unarchiveDirectoryBase=" + unarchiveDirectoryBase +
                '}';
        }
    }

    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;
    private final LanguageDefinitionManager languageDefinitionManager;
    private final SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper;
    private final Function4Throwing1<ResourceExports, ResourcesComponent, ExecContext, ResourcePath, ListView<T>, IOException> resolveFromComponent;
    private final Provider<? extends TaskDef<ResourcePath, Result<C, CE>>> configureTaskDefProvider;
    private final SerializableFunction<Result<C, CE>, Result<Option<ListView<T>>, CE>> resolveFromConfiguredLanguageDefinition;
    private final SerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<ListView<T>>, CfgRootDirectoryToObjectException>> resolveFromLanguageDefinition;
    private final String metaLanguageName;

    public ResolveDependencies(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        LanguageDefinitionManager languageDefinitionManager,
        SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper,
        Function4Throwing1<ResourceExports, ResourcesComponent, ExecContext, ResourcePath, ListView<T>, IOException> resolveFromComponent,
        Provider<? extends TaskDef<ResourcePath, Result<C, CE>>> configureTaskDefProvider,
        SerializableFunction<Result<C, CE>, Result<Option<ListView<T>>, CE>> resolveFromConfiguredLanguageDefinition,
        SerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<ListView<T>>, CfgRootDirectoryToObjectException>> resolveFromLanguageDefinition,
        String metaLanguageName
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.languageDefinitionManager = languageDefinitionManager;
        this.componentManagerWrapper = componentManagerWrapper;
        this.resolveFromComponent = resolveFromComponent;
        this.configureTaskDefProvider = configureTaskDefProvider;
        this.resolveFromConfiguredLanguageDefinition = resolveFromConfiguredLanguageDefinition;
        this.resolveFromLanguageDefinition = resolveFromLanguageDefinition;
        this.metaLanguageName = metaLanguageName;
    }

    @Override
    public Result<ListView<T>, ResolveDependenciesException> exec(ExecContext context, Input input) throws Exception {
        // TODO: need a dependency to the component manager and language definition manager; what if a language
        //       definition is added/removed, or if a language component is added/removed? That could invalidate the
        //       resolved includes!
        final ResourcePath rootDirectory = input.rootDirectory;
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, DependenciesMapping.instance)
            .mapErr(e -> ResolveDependenciesException.getConfigurationFail(rootDirectory, e))
            .flatMap(dependencies -> resolve(context, input, dependencies));
    }

    private Result<ListView<T>, ResolveDependenciesException> resolve(ExecContext context, Input input, ListView<Dependency> dependencies) {
        final ArrayList<T> resolved = new ArrayList<>();
        for(Dependency dependency : dependencies) {
            if(!dependency.kinds.contains(DependencyKind.Build)) continue;
            final Result<ListView<T>, ResolveDependenciesException> result = resolve(context, input, dependency.source);
            if(result.isErr()) {
                return result.ignoreValueIfErr();
            } else {
                // noinspection ConstantConditions (value is present because !result.isErr())
                result.get().addAllTo(resolved);
            }
        }
        return Result.ofOk(ListView.of(resolved));
    }

    private Result<ListView<T>, ResolveDependenciesException> resolve(
        ExecContext context,
        Input input,
        DependencySource source
    ) {
        return source.caseOf()
            .coordinateRequirement(coordinateRequirement -> resolve(context, source, input.unarchiveDirectoryBase, coordinateRequirement))
            .coordinate(coordinate -> resolve(context, source, input.unarchiveDirectoryBase, coordinate))
            .path(path -> Result.ofOkOrCatching(() -> resolveFromLanguageDefinition(context, source, input.rootDirectory, path), ResolveDependenciesException.class));
    }

    private Result<ListView<T>, ResolveDependenciesException> resolve(
        ExecContext context,
        DependencySource source,
        ResourcePath unarchiveDirectoryBase,
        Coordinate coordinate
    ) {
        final Class<ResolveDependenciesException> exceptionClass = ResolveDependenciesException.class;
        return languageDefinitionManager.getLanguageDefinition(coordinate)
            .mapCatching(rootDirectory -> resolveFromLanguageDefinition(context, source, rootDirectory), exceptionClass)
            .orElse(() -> componentManagerWrapper.get().getComponent(coordinate).mapCatching(component -> resolveFromComponent(context, source, unarchiveDirectoryBase, component), exceptionClass))
            .unwrapOrElse(() -> Result.ofErr(ResolveDependenciesException.languageDefinitionOrComponentNotFoundFail(source, coordinate)))
            ;
    }

    private Result<ListView<T>, ResolveDependenciesException> resolve(
        ExecContext context,
        DependencySource source,
        ResourcePath unarchiveDirectoryBase,
        CoordinateRequirement coordinateRequirement
    ) {
        final Class<ResolveDependenciesException> exceptionClass = ResolveDependenciesException.class;
        return languageDefinitionManager.getOneLanguageDefinition(coordinateRequirement)
            .mapCatching(rootDirectory -> resolveFromLanguageDefinition(context, source, rootDirectory), exceptionClass)
            .orElse(() -> componentManagerWrapper.get().getOneComponent(coordinateRequirement).mapCatching(component -> resolveFromComponent(context, source, unarchiveDirectoryBase, component), exceptionClass))
            .unwrapOrElse(() -> Result.ofErr(ResolveDependenciesException.languageDefinitionOrComponentNotFoundOrMultipleFail(source, coordinateRequirement)))
            ;
    }


    private ListView<T> resolveFromComponent(
        ExecContext context,
        DependencySource source,
        ResourcePath unarchiveDirectoryBase,
        Component component
    ) throws ResolveDependenciesException {
        final Coordinate coordinate = component.getCoordinate();
        final ResourceExports resourceExports = component.getLanguageComponent().map(lc -> lc.getLanguageInstance().getResourceExports())
            .unwrapOrElseThrow(() -> ResolveDependenciesException.noResourcesComponentFail(source, coordinate));
        final ResourcesComponent resourcesComponent = component.getResourcesComponent()
            .unwrapOrElseThrow(() -> ResolveDependenciesException.noLanguageComponentFail(source, coordinate));
        try {
            return resolveFromComponent.apply(resourceExports, resourcesComponent, context, unarchiveDirectoryBase);
        } catch(NoResourceExportsException e) {
            // TODO: this should throw an exception when a dependency is explicitly configured to resolve exports to imports of the meta-language.
            // throw ResolveDependenciesException.noResourceExportsFail(source, coordinate, metaLanguageName);
            return ListView.of();
        } catch(IOException e) {
            throw ResolveDependenciesException.resolveFromComponentFail(source, coordinate, e);
        } catch(UncheckedIOException e) {
            throw ResolveDependenciesException.resolveFromComponentFail(source, coordinate, e.getCause());
        }
    }


    private ListView<T> resolveFromLanguageDefinition(
        ExecContext context,
        DependencySource source,
        ResourcePath dependencySourceContext,
        String path
    ) throws ResolveDependenciesException {
        return resolveFromLanguageDefinition(context, source, dependencySourceContext.appendOrReplaceWithPath(path).getNormalized());
    }

    private ListView<T> resolveFromLanguageDefinition(
        ExecContext context,
        DependencySource source,
        ResourcePath rootDirectory
    ) throws ResolveDependenciesException {
        final ArrayList<T> resolved = new ArrayList<>();
        context.requireMapping(configureTaskDefProvider.get().createTask(rootDirectory), resolveFromConfiguredLanguageDefinition)
            .mapErr(e -> ResolveDependenciesException.configureFail(source, rootDirectory, metaLanguageName, e))
            .mapThrowing(o -> o.unwrapOrElseThrow(() -> ResolveDependenciesException.noConfigurationFail(source, rootDirectory, metaLanguageName)))
            .unwrap()
            .addAllTo(resolved);
        context.requireMapping(cfgRootDirectoryToObject, rootDirectory, resolveFromLanguageDefinition)
            .mapErr(e -> ResolveDependenciesException.getConfigurationFail(rootDirectory, e))
            .mapThrowing(o -> o.unwrapOrElseThrow(() -> ResolveDependenciesException.noConfigurationFail(source, rootDirectory, metaLanguageName)))
            .unwrap()
            .addAllTo(resolved);
        return ListView.of(resolved);
    }
}
