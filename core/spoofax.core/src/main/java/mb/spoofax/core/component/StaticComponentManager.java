package mb.spoofax.core.component;

import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.MultiMapView;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.PieBuilder;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StaticComponentManager implements ComponentManager {
    public final LoggerComponent loggerComponent;
    public final PlatformComponent platformComponent;

    private final ListView<StandaloneComponent> standaloneComponents;
    private final MapView<String, GroupedComponents> groupedComponents;

    // Following fields are kept for use by dynamic component managers.
    public final ResourceServiceComponent baseResourceServiceComponent;
    public final Supplier<PieBuilder> pieBuilderSupplier;
    public final ListView<ResourceRegistriesProvider> globalResourceRegistryProviders;
    public final ListView<TaskDefsProvider> globalTaskDefsProviders;
    public final ListView<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers;
    public final MultiMapView<String, Consumer<ResourceServiceModule>> groupedResourceServiceModuleCustomizers;
    public final ListView<Consumer<RootPieModule>> pieModuleCustomizers;
    public final MultiMapView<String, Consumer<RootPieModule>> groupedPieModuleCustomizers;


    StaticComponentManager(
        LoggerComponent loggerComponent,
        PlatformComponent platformComponent,

        ListView<StandaloneComponent> standaloneComponents,
        MapView<String, GroupedComponents> groupedComponents,

        ResourceServiceComponent baseResourceServiceComponent,
        Supplier<PieBuilder> pieBuilderSupplier,
        ListView<ResourceRegistriesProvider> globalResourceRegistryProviders,
        ListView<TaskDefsProvider> globalTaskDefsProviders,
        ListView<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers,
        MultiMapView<String, Consumer<ResourceServiceModule>> groupedResourceServiceModuleCustomizers,
        ListView<Consumer<RootPieModule>> pieModuleCustomizers,
        MultiMapView<String, Consumer<RootPieModule>> groupedPieModuleCustomizers
    ) {
        this.loggerComponent = loggerComponent;
        this.platformComponent = platformComponent;

        this.standaloneComponents = standaloneComponents;
        this.groupedComponents = groupedComponents;

        this.baseResourceServiceComponent = baseResourceServiceComponent;
        this.pieBuilderSupplier = pieBuilderSupplier;
        this.globalResourceRegistryProviders = globalResourceRegistryProviders;
        this.globalTaskDefsProviders = globalTaskDefsProviders;
        this.resourceServiceModuleCustomizers = resourceServiceModuleCustomizers;
        this.groupedResourceServiceModuleCustomizers = groupedResourceServiceModuleCustomizers;
        this.pieModuleCustomizers = pieModuleCustomizers;
        this.groupedPieModuleCustomizers = groupedPieModuleCustomizers;
    }

    @Override public void close() {
        groupedComponents.values().forEach(GroupedComponents::close);
        standaloneComponents.forEach(StandaloneComponent::close);
    }


    @Override public LoggerComponent getLoggerComponent() {
        return loggerComponent;
    }

    @Override public PlatformComponent getPlatformComponent() {
        return platformComponent;
    }


    @Override public @Nullable Component getComponent(Coordinate coordinate) {
        for(StandaloneComponent component : standaloneComponents) {
            if(component.participant.getCoordinates().equals(coordinate)) {
                return component;
            }
        }
        for(GroupedComponents component : groupedComponents.values()) {
            for(Participant participant : component.participants) {
                if(participant.getCoordinates().equals(coordinate)) {
                    return component;
                }
            }
        }
        return null;
    }

    @Override public @Nullable LanguageComponent getLanguageComponent(Coordinate coordinate) {
        for(StandaloneComponent component : standaloneComponents) {
            final @Nullable LanguageComponent languageComponent = component.getLanguageComponent(coordinate);
            if(languageComponent != null) return languageComponent;
        }
        for(GroupedComponents component : groupedComponents.values()) {
            final @Nullable LanguageComponent languageComponent = component.getLanguageComponent(coordinate);
            if(languageComponent != null) return languageComponent;
        }
        return null;
    }
}
