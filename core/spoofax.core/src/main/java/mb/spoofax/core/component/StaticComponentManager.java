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
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StaticComponentManager<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> implements ComponentManager {
    public final L loggerComponent;
    public final P platformComponent;

    private final MapView<Coordinate, StandaloneComponent<L, R, P>> standaloneComponents;
    private final MapView<String, GroupedComponents<L, R, P>> groupedComponents;

    // Following fields are kept for use by dynamic component managers.
    public final R baseResourceServiceComponent;
    public final Supplier<PieBuilder> pieBuilderSupplier;
    public final ListView<ResourceRegistriesProvider> globalResourceRegistryProviders;
    public final ListView<TaskDefsProvider> globalTaskDefsProviders;
    public final ListView<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers;
    public final MultiMapView<String, Consumer<ResourceServiceModule>> groupedResourceServiceModuleCustomizers;
    public final ListView<Consumer<RootPieModule>> pieModuleCustomizers;
    public final MultiMapView<String, Consumer<RootPieModule>> groupedPieModuleCustomizers;


    StaticComponentManager(
        L loggerComponent,
        P platformComponent,

        MapView<Coordinate, StandaloneComponent<L, R, P>> standaloneComponents,
        MapView<String, GroupedComponents<L, R, P>> groupedComponents,

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

    @Override
    public void close() {
        groupedComponents.values().forEach(GroupedComponents::close);
        standaloneComponents.values().forEach(StandaloneComponent::close);
    }


    @Override
    public LoggerComponent getLoggerComponent() {
        return loggerComponent;
    }

    @Override
    public PlatformComponent getPlatformComponent() {
        return platformComponent;
    }


    @Override
    public Option<? extends Component> getComponent(Coordinate coordinate) {
        final @Nullable Component standaloneComponent = standaloneComponents.get(coordinate);
        if(standaloneComponent != null) {
            return Option.ofSome(standaloneComponent);
        }
        for(GroupedComponents<L, R, P> component : groupedComponents.values()) {
            if(component.languageComponents.keySet().contains(coordinate)) {
                return Option.ofSome(component);
            }
        }
        return Option.ofNone();
    }

    @Override
    public CollectionView<Component> getComponents(CoordinateRequirement coordinateRequirement) {
        final ArrayList<Component> components = new ArrayList<>();
        for(StandaloneComponent<L, R, P> component : standaloneComponents.values()) {
            if(coordinateRequirement.matches(component.coordinate)) {
                components.add(component);
            }
        }
        for(GroupedComponents<L, R, P> component : groupedComponents.values()) {
            for(Coordinate coordinate : component.languageComponents.keySet()) {
                if(coordinateRequirement.matches(coordinate)) {
                    components.add(component);
                }
            }

        }
        return CollectionView.of(components);
    }


    @Override
    public Option<StandaloneComponent<?, ?, ?>> getStandaloneComponent(Coordinate coordinate) {
        return Option.ofNullable(standaloneComponents.get(coordinate));
    }

    @Override public MapView<Coordinate, ? extends StandaloneComponent<?, ?, ?>> getStandaloneComponents() {
        return standaloneComponents;
    }

    @Override
    public Option<GroupedComponents<?, ?, ?>> getGroupedComponents(String group) {
        return Option.ofNullable(groupedComponents.get(group));
    }

    @Override public MapView<String, ? extends GroupedComponents<?, ?, ?>> getGroupedComponents() {
        return groupedComponents;
    }


    @Override
    public Option<LanguageComponent> getLanguageComponent(Coordinate coordinate) {
        for(StandaloneComponent<L, R, P> component : standaloneComponents.values()) {
            final Option<LanguageComponent> languageComponent = component.getLanguageComponent(coordinate);
            if(languageComponent.isSome()) return languageComponent;
        }
        for(GroupedComponents<L, R, P> component : groupedComponents.values()) {
            final Option<LanguageComponent> languageComponent = component.getLanguageComponent(coordinate);
            if(languageComponent.isSome()) return languageComponent;
        }
        return Option.ofNone();
    }

    @Override
    public CollectionView<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement) {
        final ArrayList<LanguageComponent> languageComponents = new ArrayList<>();
        for(StandaloneComponent<L, R, P> component : standaloneComponents.values()) {
            component.getLanguageComponents().values().addAllTo(languageComponents);
        }
        for(GroupedComponents<L, R, P> component : groupedComponents.values()) {
            component.getLanguageComponents().values().addAllTo(languageComponents);
        }
        return CollectionView.of(languageComponents);
    }
}
