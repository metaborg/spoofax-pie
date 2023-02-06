package mb.spoofax.core.component;

import mb.common.util.ListView;
import mb.common.util.MultiMapView;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.PieBuilder;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.spoofax.core.platform.PlatformComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface StaticComponentManager extends ComponentManager {
    LoggerComponent getLoggerComponent();

    ResourceServiceComponent getBaseResourceServiceComponent();

    PlatformComponent getPlatformComponent();

    Supplier<PieBuilder> getPieBuilderSupplier();


    ListView<ResourceRegistriesProvider> getGlobalResourceRegistryProviders();

    ListView<TaskDefsProvider> getGlobalTaskDefsProviders();

    ListView<Consumer<ResourceServiceModule>> getResourceServiceModuleCustomizers();

    MultiMapView<String, Consumer<ResourceServiceModule>> getGroupedResourceServiceModuleCustomizers();

    ListView<Consumer<RootPieModule>> getPieModuleCustomizers();

    MultiMapView<String, Consumer<RootPieModule>> getGroupedPieModuleCustomizers();
}
