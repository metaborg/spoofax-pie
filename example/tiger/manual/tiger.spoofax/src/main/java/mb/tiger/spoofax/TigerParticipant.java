package mb.tiger.spoofax;

import mb.common.util.ListView;
import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.Version;
import mb.spoofax.core.component.ComponentDependencyResolver;
import mb.spoofax.core.component.ComponentManager;
import mb.spoofax.core.component.Participant;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.core.component.SubcomponentRegistry;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

public class TigerParticipant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> implements Participant<L, R, P> {
    protected @Nullable TigerResourcesComponent resourcesComponent;
    protected @Nullable TigerComponent component;


    @Override
    public Coordinate getCoordinate() {
        return new Coordinate(
            "org.metaborg",
            "tiger",
            new Version(0, 1, 0)
        );
    }

    @Override
    public ListView<CoordinateRequirement> getDependencies() {
        return ListView.of();
    }

    @Override
    public @Nullable String getCompositionGroup() {
        return null;
    }

    @Override
    public ListView<String> getLanguageFileExtensions() {
        return ListView.copyOf(TigerInstance.extensions);
    }


    @Override
    public @Nullable ResourceRegistriesProvider getGlobalResourceRegistriesProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        return null;
    }

    @Override
    public @Nullable TaskDefsProvider getGlobalTaskDefsProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        return null;
    }


    protected TigerResourcesModule createResourcesModule(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent
    ) {
        return new TigerResourcesModule();
    }

    protected void customizeResourcesModule(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        TigerResourcesModule module
    ) {}

    protected void customizeResourcesComponentBuilder(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        DaggerTigerResourcesComponent.Builder builder
    ) {}

    @Override
    public @Nullable TigerResourcesComponent getResourceRegistriesProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        final TigerResourcesComponent resourcesComponent = getResourcesComponent(
            loggerComponent,
            baseResourceServiceComponent,
            platformComponent,
            subcomponentRegistry,
            dependencyResolver
        );
        subcomponentRegistry.register(TigerResourcesComponent.class, resourcesComponent);
        return resourcesComponent;
    }

    @Override
    public TigerResourcesComponent getResourcesComponent(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        if(resourcesComponent == null) {
            final TigerResourcesModule module = createResourcesModule(loggerComponent, baseResourceServiceComponent, platformComponent);
            customizeResourcesModule(loggerComponent, baseResourceServiceComponent, platformComponent, module);
            final DaggerTigerResourcesComponent.Builder builder = DaggerTigerResourcesComponent.builder()
                .tigerResourcesModule(module);
            customizeResourcesComponentBuilder(loggerComponent, baseResourceServiceComponent, platformComponent, builder);
            resourcesComponent = builder.build();
            subcomponentRegistry.register(TigerResourcesComponent.class, resourcesComponent);
        }
        return resourcesComponent;
    }

    @Override public @Nullable Consumer<ResourceServiceModule> getResourceServiceModuleCustomizer() {
        return null;
    }


    @Override
    public TigerComponent getTaskDefsProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        final TigerComponent languageComponent = getLanguageComponent(
            loggerComponent,
            baseResourceServiceComponent,
            resourceServiceComponent,
            platformComponent,
            subcomponentRegistry,
            dependencyResolver
        );
        subcomponentRegistry.register(TigerComponent.class, languageComponent);
        return languageComponent;
    }


    protected TigerModule createModule(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent
    ) {
        return new TigerModule();
    }

    protected void customizeModule(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        TigerModule module
    ) {}

    @Override
    public TigerComponent getLanguageComponent(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        if(component == null) {
            final TigerModule module = createModule(loggerComponent, baseResourceServiceComponent, resourceServiceComponent, platformComponent);
            customizeModule(loggerComponent, baseResourceServiceComponent, resourceServiceComponent, platformComponent, module);
            final DaggerTigerComponent.Builder builder = DaggerTigerComponent.builder()
                .tigerModule(module)
                .loggerComponent(loggerComponent)
                .tigerResourcesComponent(getResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent, subcomponentRegistry, dependencyResolver))
                .resourceServiceComponent(resourceServiceComponent)
                .platformComponent(platformComponent);
            component = builder.build();
        }
        return component;
    }

    @Override
    public @Nullable Consumer<RootPieModule> getPieModuleCustomizer() {
        return null;
    }


    @Override
    public void start(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        PieComponent pieComponent,
        ComponentDependencyResolver dependencyResolver
    ) {

    }

    @Override
    public void started(
        ResourceServiceComponent resourceServiceComponent,
        PieComponent pieComponent,
        StaticComponentManager staticComponentManager,
        ComponentManager componentManager
    ) {

    }

    @Override public void close() {
        if(component != null) {
            component.close();
            component = null;
        }
        resourcesComponent = null;
    }
}
