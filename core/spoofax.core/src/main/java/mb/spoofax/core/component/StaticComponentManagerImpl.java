package mb.spoofax.core.component;

import mb.common.option.Option;
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
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.resource.ResourcesComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class StaticComponentManagerImpl<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> implements StaticComponentManager {
    public final L loggerComponent;
    public final P platformComponent;

    private final MapView<Coordinate, ComponentImpl> componentsByCoordinate;
    private final MapView<String, ComponentGroupImpl> componentGroups;

    // Following fields are kept for use by dynamic component managers.
    public final R baseResourceServiceComponent;
    public final Supplier<PieBuilder> pieBuilderSupplier;
    public final ListView<ResourceRegistriesProvider> globalResourceRegistryProviders;
    public final ListView<TaskDefsProvider> globalTaskDefsProviders;
    public final ListView<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers;
    public final MultiMapView<String, Consumer<ResourceServiceModule>> groupedResourceServiceModuleCustomizers;
    public final ListView<Consumer<RootPieModule>> pieModuleCustomizers;
    public final MultiMapView<String, Consumer<RootPieModule>> groupedPieModuleCustomizers;


    StaticComponentManagerImpl(
        L loggerComponent,
        P platformComponent,

        ArrayList<ComponentImpl> componentsList,
        ArrayList<ComponentGroupImpl> componentGroupsList,

        R baseResourceServiceComponent,
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

        final LinkedHashMap<Coordinate, ComponentImpl> componentsByCoordinate = new LinkedHashMap<>();
        for(ComponentImpl component : componentsList) {
            componentsByCoordinate.put(component.getCoordinate(), component);
        }
        final Map<String, ComponentGroupImpl> componentGroups = new LinkedHashMap<>();
        for(ComponentGroupImpl componentGroup : componentGroupsList) {
            componentGroups.put(componentGroup.getGroup(), componentGroup);
        }

        this.componentsByCoordinate = MapView.of(componentsByCoordinate);
        this.componentGroups = MapView.of(componentGroups);

        this.baseResourceServiceComponent = baseResourceServiceComponent;
        this.pieBuilderSupplier = pieBuilderSupplier;
        this.globalResourceRegistryProviders = globalResourceRegistryProviders;
        this.globalTaskDefsProviders = globalTaskDefsProviders;
        this.resourceServiceModuleCustomizers = resourceServiceModuleCustomizers;
        this.groupedResourceServiceModuleCustomizers = groupedResourceServiceModuleCustomizers;
        this.pieModuleCustomizers = pieModuleCustomizers;
        this.groupedPieModuleCustomizers = groupedPieModuleCustomizers;

        for(ComponentImpl component : componentsList) {
            component.started(this, this);
        }
    }

    @Override
    public void close() {
        componentsByCoordinate.values().forEach(ComponentImpl::close);
        componentGroups.values().forEach(ComponentGroupImpl::close);
    }


    // Components

    @Override
    public Option<? extends Component> getComponent(Coordinate coordinate) {
        final @Nullable Component component = componentsByCoordinate.get(coordinate);
        return Option.ofNullable(component);
    }

    @Override
    public Stream<? extends Component> getComponents() {
        return componentsByCoordinate.values().stream();
    }

    @Override
    public Stream<? extends Component> getComponents(CoordinateRequirement coordinateRequirement) {
        return componentsByCoordinate.values().stream().filter(c -> c.matchesCoordinate(coordinateRequirement));
    }

    @Override
    public Option<? extends ComponentGroup> getComponentGroup(String group) {
        return Option.ofNullable(componentGroups.get(group));
    }

    @Override
    public MapView<String, ? extends ComponentGroup> getComponentGroups() {
        return componentGroups;
    }


    // Resources subcomponents

    @Override
    public Option<ResourcesComponent> getResourcesComponent(Coordinate coordinate) {
        for(ComponentImpl component : componentsByCoordinate.values()) {
            final Option<ResourcesComponent> resourcesComponent = component.getResourcesComponent();
            if(resourcesComponent.isSome()) return resourcesComponent;
        }
        return Option.ofNone();
    }

    @Override
    public Stream<ResourcesComponent> getResourcesComponents(CoordinateRequirement coordinateRequirement) {
        return componentsByCoordinate.values().stream()
            .filter(c -> c.matchesCoordinate(coordinateRequirement))
            .flatMap(c -> c.getResourcesComponent().stream());
    }


    // Language subcomponents

    @Override
    public Option<LanguageComponent> getLanguageComponent(Coordinate coordinate) {
        for(ComponentImpl component : componentsByCoordinate.values()) {
            final Option<LanguageComponent> languageComponent = component.getLanguageComponent();
            if(languageComponent.isSome()) return languageComponent;
        }
        return Option.ofNone();
    }

    @Override
    public Stream<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement) {
        return componentsByCoordinate.values().stream()
            .filter(c -> c.matchesCoordinate(coordinateRequirement))
            .flatMap(c -> c.getLanguageComponent().stream());
    }


    // Typed subcomponents

    @Override
    public <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType) {
        final @Nullable ComponentImpl component = componentsByCoordinate.get(coordinate);
        if(component != null) {
            return component.getSubcomponent(subcomponentType);
        }
        return Option.ofNone();
    }

    @Override
    public <T> Stream<T> getSubcomponents(Class<T> subcomponentType) {
        return componentsByCoordinate.values().stream()
            .flatMap(c -> c.getSubcomponent(subcomponentType).stream());
    }

    @Override
    public <T> Stream<T> getSubcomponents(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType) {
        return componentsByCoordinate.values().stream()
            .filter(c -> c.matchesCoordinate(coordinateRequirement))
            .flatMap(c -> c.getSubcomponent(subcomponentType).stream());
    }


    // Information for dynamic component managers

    @Override
    public LoggerComponent getLoggerComponent() {
        return loggerComponent;
    }

    @Override
    public ResourceServiceComponent getBaseResourceServiceComponent() {
        return baseResourceServiceComponent;
    }

    @Override
    public PlatformComponent getPlatformComponent() {
        return platformComponent;
    }

    @Override
    public Supplier<PieBuilder> getPieBuilderSupplier() {
        return pieBuilderSupplier;
    }


    @Override
    public ListView<ResourceRegistriesProvider> getGlobalResourceRegistryProviders() {
        return globalResourceRegistryProviders;
    }

    @Override
    public ListView<TaskDefsProvider> getGlobalTaskDefsProviders() {
        return globalTaskDefsProviders;
    }

    @Override
    public ListView<Consumer<ResourceServiceModule>> getResourceServiceModuleCustomizers() {
        return resourceServiceModuleCustomizers;
    }

    @Override
    public MultiMapView<String, Consumer<ResourceServiceModule>> getGroupedResourceServiceModuleCustomizers() {
        return groupedResourceServiceModuleCustomizers;
    }

    @Override
    public ListView<Consumer<RootPieModule>> getPieModuleCustomizers() {
        return pieModuleCustomizers;
    }

    @Override
    public MultiMapView<String, Consumer<RootPieModule>> getGroupedPieModuleCustomizers() {
        return groupedPieModuleCustomizers;
    }
}
