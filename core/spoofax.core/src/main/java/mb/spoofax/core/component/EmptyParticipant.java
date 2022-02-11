package mb.spoofax.core.component;

import mb.common.util.ListView;
import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

public abstract class EmptyParticipant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> implements Participant<L, R, P> {
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
        return ListView.of();
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


    @Override
    public @Nullable ResourceRegistriesProvider getResourceRegistriesProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        return null;
    }

    @Override
    public @Nullable Consumer<ResourceServiceModule> getResourceServiceModuleCustomizer() {
        return null;
    }


    @Override
    public @Nullable TaskDefsProvider getTaskDefsProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        return null;
    }

    @Override
    public @Nullable LanguageComponent getLanguageComponent(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        return null;
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
    public void close() {

    }
}
