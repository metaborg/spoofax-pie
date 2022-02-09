package mb.spoofax.core.component;

import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

public abstract class EmptyParticipant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> implements Participant<L, R, P> {
    @Override
    public ListView<ComponentDependency> getDependencies() {
        return ListView.of();
    }

    @Override
    public @Nullable String getGroup() {
        return null;
    }

    @Override
    public ListView<String> getLanguageFileExtensions() {
        return ListView.of();
    }


    @Override
    public @Nullable ResourceRegistriesProvider getGlobalResourceRegistriesProvider(L loggerComponent, R baseResourceServiceComponent, P platformComponent) {
        return null;
    }

    @Override
    public @Nullable TaskDefsProvider getGlobalTaskDefsProvider(L loggerComponent, ResourceServiceComponent resourceServiceComponent, P platformComponent) {
        return null;
    }


    @Override
    public @Nullable ResourceRegistriesProvider getResourceRegistriesProvider(L loggerComponent, R baseResourceServiceComponent, P platformComponent) {
        return null;
    }

    @Override
    public @Nullable Consumer<ResourceServiceModule> getResourceServiceModuleCustomizer() {
        return null;
    }


    @Override
    public @Nullable TaskDefsProvider getTaskDefsProvider(L loggerComponent, R baseResourceServiceComponent, ResourceServiceComponent resourceServiceComponent, P platformComponent) {
        return null;
    }

    @Override
    public @Nullable LanguageComponent getLanguageComponent(L loggerComponent, R baseResourceServiceComponent, ResourceServiceComponent resourceServiceComponent, P platformComponent) {
        return null;
    }

    @Override
    public @Nullable Consumer<RootPieModule> getPieModuleCustomizer() {
        return null;
    }


    @Override
    public MapView<Class<?>, Object> getSubcomponents(L loggerComponent, R baseResourceServiceComponent, ResourceServiceComponent resourceServiceComponent, P platformComponent, PieComponent pieComponent) {
        return MapView.of();
    }


    @Override
    public void start(L loggerComponent, R baseResourceServiceComponent, ResourceServiceComponent resourceServiceComponent, P platformComponent, PieComponent pieComponent) {

    }

    @Override
    public void close() {

    }
}
