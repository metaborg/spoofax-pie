package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StaticComponentManager implements ComponentManager {
    public final LoggerComponent loggerComponent;
    public final PlatformComponent platformComponent;

    private final MapView<Coordinate, ComponentImpl> componentsByCoordinate;
    private final MapView<String, ComponentGroupImpl> componentGroups;

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

        ArrayList<ComponentImpl> componentsList,
        ArrayList<ComponentGroupImpl> componentGroupsList,

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
    }

    @Override
    public void close() {
        componentsByCoordinate.values().forEach(ComponentImpl::close);
        componentGroups.values().forEach(ComponentGroupImpl::close);
    }


    @Override
    public LoggerComponent getLoggerComponent() {
        return loggerComponent;
    }

    @Override
    public PlatformComponent getPlatformComponent() {
        return platformComponent;
    }


    // Components

    @Override
    public Option<? extends Component> getComponent(Coordinate coordinate) {
        final @Nullable Component component = componentsByCoordinate.get(coordinate);
        return Option.ofNullable(component);
    }

    @Override
    public CollectionView<? extends Component> getComponents(CoordinateRequirement coordinateRequirement) {
        return componentsByCoordinate.values();
    }

    @Override
    public Option<? extends ComponentGroup> getComponentGroup(String group) {
        return Option.ofNullable(componentGroups.get(group));
    }

    @Override
    public MapView<String, ? extends ComponentGroup> getComponentGroups() {
        return componentGroups;
    }


    // Language components (of components)

    @Override
    public Option<LanguageComponent> getLanguageComponent(Coordinate coordinate) {
        for(ComponentImpl component : componentsByCoordinate.values()) {
            final Option<LanguageComponent> languageComponent = component.getLanguageComponent();
            if(languageComponent.isSome()) return languageComponent;
        }
        return Option.ofNone();
    }

    @Override
    public CollectionView<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement) {
        final ArrayList<LanguageComponent> languageComponents = new ArrayList<>();
        for(ComponentImpl component : componentsByCoordinate.values()) {
            component.getLanguageComponent().ifSome(languageComponent -> {
                if(coordinateRequirement.matches(component.coordinate)) {
                    languageComponents.add(languageComponent);
                }
            });
        }
        return CollectionView.of(languageComponents);
    }


    // Typed subcomponents

    @Override
    public <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentClass) {
        final @Nullable ComponentImpl component = componentsByCoordinate.get(coordinate);
        if(component != null) {
            return component.getSubcomponent(subcomponentClass);
        }
        return Option.ofNone();
    }

    @Override
    public <T> CollectionView<T> getSubcomponents(Class<T> subcomponentClass) {
        return CollectionView.of(componentsByCoordinate.values().stream()
            .flatMap(c -> c.getSubcomponent(subcomponentClass).stream())
        );
    }

    @Override
    public <T> CollectionView<T> getSubcomponents(CoordinateRequirement coordinateRequirement, Class<T> subcomponentClass) {
        return CollectionView.of(componentsByCoordinate.values().stream()
            .filter(c -> c.matchesCoordinate(coordinateRequirement))
            .flatMap(c -> c.getSubcomponent(subcomponentClass).stream())
        );
    }
}
