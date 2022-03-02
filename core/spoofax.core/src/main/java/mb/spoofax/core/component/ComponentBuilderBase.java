package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.MultiMap;
import mb.common.util.MultiMapView;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.PieBuilder;
import mb.pie.api.serde.JavaSerde;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.pie.graph.DefaultEdge;
import mb.pie.graph.DirectedAcyclicGraph;
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class ComponentBuilderBase<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> {
    protected final L loggerComponent;
    protected final R baseResourceServiceComponent;
    protected final P platformComponent;
    protected final Supplier<PieBuilder> pieBuilderSupplier;


    protected ComponentBuilderBase(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        Supplier<PieBuilder> pieBuilderSupplier
    ) {
        this.loggerComponent = loggerComponent;
        this.baseResourceServiceComponent = baseResourceServiceComponent;
        this.platformComponent = platformComponent;
        this.pieBuilderSupplier = pieBuilderSupplier;
    }


    /**
     * Builds {@link ComponentImpl}s and {@link ComponentGroupImpl} from {@link Participant}s.
     */
    protected BuildResult build(
        Iterable<Participant<L, R, P>> participants,
        ListView<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers,
        MultiMapView<String, Consumer<ResourceServiceModule>> groupedResourceServiceModuleCustomizers,
        ListView<Consumer<RootPieModule>> pieModuleCustomizers,
        MultiMapView<String, Consumer<RootPieModule>> groupedPieModuleCustomizers,
        @Nullable ClassLoader classLoader
    ) {
        // Check for duplicate coordinates.
        // TODO: what about duplicate coordinates of a dynamic component with already existing static components?
        final HashSet<Coordinate> coordinates = new HashSet<>();
        for(Participant<L, R, P> participant : participants) {
            final Coordinate coordinate = participant.getCoordinate();
            if(coordinates.contains(coordinate)) {
                throw new IllegalStateException("Cannot build components, there are multiple participants with coordinate '" + coordinate + "'");
            }
            coordinates.add(coordinate);
        }

        // Sort participants in dependency order using a DAG.
        final DirectedAcyclicGraph<Participant<L, R, P>, DefaultEdge> participantsGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        for(Participant<L, R, P> participant : participants) {
            participantsGraph.addVertex(participant);
        }
        for(Participant<L, R, P> source : participants) {
            for(CoordinateRequirement dependency : source.getDependencies()) {
                boolean dependencyFound = false;
                for(Participant<L, R, P> target : participants) {
                    if(!source.equals(target) && dependency.matches(target.getCoordinate())) {
                        participantsGraph.addEdge(target, source);
                        dependencyFound = true;
                    }
                }
                if(!dependencyFound) {
                    throw new IllegalStateException("Cannot build components, participant with coordinate '" + source + "' depends on participant with coordinate requirement '" + dependency + "', but that participant was not found");
                }
            }
        }

        // Create builders for participants.
        final ArrayList<ComponentBuilder> builders = StreamSupport.stream(participantsGraph.spliterator(), false)
            .map(ComponentBuilder::new)
            .collect(Collectors.toCollection(ArrayList::new));
        final ArrayList<ComponentBuilder> standaloneBuilders = builders.stream()
            .filter(b -> b.group == null)
            .collect(Collectors.toCollection(ArrayList::new));
        final MultiMap<String, ComponentBuilder> groupedBuilders = MultiMap.withLinkedHash();
        for(ComponentBuilder builder : builders) {
            if(builder.group != null) {
                groupedBuilders.put(builder.group, builder);
            }
        }
        final DependencyResolver dependencyResolver = new DependencyResolver(builders);

        // Gather global resource registry providers.
        final ArrayList<ResourceRegistriesProvider> globalResourceRegistryProviders = new ArrayList<>();
        builders.forEach(b -> addToGlobalResourceRegistryProviders(b, globalResourceRegistryProviders, dependencyResolver));

        // Create resource registry providers.
        builders.forEach(b -> b.resourceRegistriesProvider = b.participant.getResourceRegistriesProvider(
            loggerComponent,
            baseResourceServiceComponent,
            platformComponent,
            b,
            dependencyResolver
        ));

        // Create resource service components.
        standaloneBuilders.forEach(b -> b.resourceServiceComponent = createResourceComponent(
            b.asSingleton(),
            globalResourceRegistryProviders.stream(),
            resourceServiceModuleCustomizers.stream()
        ));
        groupedBuilders.forEach((g, bs) -> {
            final ResourceServiceComponent resourceServiceComponent = createResourceComponent(
                bs,
                globalResourceRegistryProviders.stream(),
                Stream.concat(resourceServiceModuleCustomizers.stream(), groupedResourceServiceModuleCustomizers.get(g).stream())
            );
            bs.forEach(b -> b.resourceServiceComponent = resourceServiceComponent);
        });

        // Gather global task definition providers.
        final ArrayList<TaskDefsProvider> globalTaskDefsProviders = new ArrayList<>();
        builders.forEach(b -> addToGlobalTaskDefsProvider(b, globalTaskDefsProviders, dependencyResolver));

        // Create task definition providers.
        builders.forEach(b -> b.taskDefsProvider = b.participant.getTaskDefsProvider(
            loggerComponent,
            baseResourceServiceComponent,
            b.resourceServiceComponent,
            platformComponent,
            b,
            dependencyResolver
        ));

        // Create language components.
        builders.forEach(b -> b.languageComponent = b.participant.getLanguageComponent(
            loggerComponent,
            baseResourceServiceComponent,
            b.resourceServiceComponent,
            platformComponent,
            b,
            dependencyResolver
        ));

        // Create PIE components.
        standaloneBuilders.forEach(b -> b.pieComponent = createPieComponent(
            b.asSingleton(),
            b.resourceServiceComponent,
            globalTaskDefsProviders.stream(),
            classLoader,
            pieModuleCustomizers.stream()
        ));
        groupedBuilders.forEach((g, bs) -> {
            final PieComponent pieComponent = createPieComponent(
                bs,
                bs.get(0).resourceServiceComponent, // Always contains at least one element in the list, and is the same between all components in the same group, so this is safe.
                globalTaskDefsProviders.stream(),
                classLoader,
                Stream.concat(pieModuleCustomizers.stream(), groupedPieModuleCustomizers.get(g).stream())
            );
            bs.forEach(b -> b.pieComponent = pieComponent);
        });

        // Start all components.
        builders.forEach(b -> b.participant.start(
            loggerComponent,
            baseResourceServiceComponent,
            b.resourceServiceComponent,
            platformComponent,
            b.pieComponent,
            dependencyResolver
        ));

        // Gather components.
        final ArrayList<ComponentImpl> components = new ArrayList<>(builders.size());
        final MultiMap<String, ComponentImpl> componentsPerGroup = MultiMap.withLinkedHash();
        for(ComponentBuilder builder : builders) {
            final ComponentImpl component = builder.toComponent();
            components.add(component);
            if(builder.group != null) {
                componentsPerGroup.put(builder.group, component);
            }
        }
        final ArrayList<ComponentGroupImpl> componentGroups = new ArrayList<>(componentsPerGroup.keySet().size());
        for(Map.Entry<String, ArrayList<ComponentImpl>> entry : componentsPerGroup.entrySet()) {
            // Always contains at least one element in the list, and ResourceService/PIE components are the same between all components in the same group, so this is safe.
            final ComponentImpl firstComponent = entry.getValue().get(0);
            final LinkedHashMap<Coordinate, ComponentImpl> componentsPerCoordinate = new LinkedHashMap<>();
            entry.getValue().forEach(c -> componentsPerCoordinate.put(c.getCoordinate(), c));
            final ComponentGroupImpl componentGroup = new ComponentGroupImpl(entry.getKey(), firstComponent.getResourceServiceComponent(), firstComponent.getPieComponent(), MapView.of(componentsPerCoordinate));
            componentGroup.components.values().addAllTo(components);
            componentGroups.add(componentGroup);
        }

        return new BuildResult(
            globalResourceRegistryProviders,
            globalTaskDefsProviders,
            components,
            componentGroups
        );
    }

    protected static class BuildResult {
        public final ArrayList<ResourceRegistriesProvider> globalResourceRegistryProviders;
        public final ArrayList<TaskDefsProvider> globalTaskDefsProviders;
        public final ArrayList<ComponentImpl> components;
        public final ArrayList<ComponentGroupImpl> componentGroups;

        private BuildResult(
            ArrayList<ResourceRegistriesProvider> globalResourceRegistryProviders,
            ArrayList<TaskDefsProvider> globalTaskDefsProviders,
            ArrayList<ComponentImpl> components,
            ArrayList<ComponentGroupImpl> componentGroups
        ) {
            this.globalResourceRegistryProviders = globalResourceRegistryProviders;
            this.globalTaskDefsProviders = globalTaskDefsProviders;
            this.components = components;
            this.componentGroups = componentGroups;
        }
    }


    /**
     * Builds one {@link ComponentImpl} from one {@link Participant}. The group of the participant is ignored.
     */
    protected BuildOneResult buildOne(
        Participant<L, R, P> participant,
        Stream<ResourceRegistriesProvider> additionalResourceRegistriesProviders,
        Stream<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers,
        Stream<TaskDefsProvider> additionalTaskDefsProviders,
        Stream<Consumer<RootPieModule>> pieModuleCustomizers,
        @Nullable ClassLoader classLoader
    ) {
        final ComponentBuilder builder = new ComponentBuilder(participant);
        final ComponentDependencyResolver dependencyResolver = new NullResolver();
        builder.resourceRegistriesProvider = participant.getResourceRegistriesProvider(
            loggerComponent,
            baseResourceServiceComponent,
            platformComponent,
            builder,
            dependencyResolver
        );
        builder.resourceServiceComponent = createResourceComponent(
            builder.asSingleton(),
            additionalResourceRegistriesProviders,
            resourceServiceModuleCustomizers
        );
        builder.taskDefsProvider = participant.getTaskDefsProvider(
            loggerComponent,
            baseResourceServiceComponent,
            builder.resourceServiceComponent,
            platformComponent,
            builder,
            dependencyResolver
        );
        builder.languageComponent = participant.getLanguageComponent(
            loggerComponent,
            baseResourceServiceComponent,
            builder.resourceServiceComponent,
            platformComponent,
            builder,
            dependencyResolver
        );
        builder.pieComponent = createPieComponent(
            builder.asSingleton(),
            builder.resourceServiceComponent,
            additionalTaskDefsProviders,
            classLoader,
            pieModuleCustomizers
        );
        participant.start(
            loggerComponent,
            baseResourceServiceComponent,
            builder.resourceServiceComponent,
            platformComponent,
            builder.pieComponent,
            dependencyResolver
        );
        return new BuildOneResult(
            participant.getGlobalResourceRegistriesProvider(
                loggerComponent,
                baseResourceServiceComponent,
                platformComponent,
                builder,
                dependencyResolver
            ),
            participant.getGlobalTaskDefsProvider(
                loggerComponent,
                baseResourceServiceComponent,
                builder.resourceServiceComponent,
                platformComponent,
                builder,
                dependencyResolver
            ),
            builder.toComponent(true)
        );
    }

    protected static class BuildOneResult {
        public final @Nullable ResourceRegistriesProvider globalResourceRegistryProvider;
        public final @Nullable TaskDefsProvider globalTaskDefsProvider;
        public final ComponentImpl component;

        public BuildOneResult(
            @Nullable ResourceRegistriesProvider globalResourceRegistryProvider,
            @Nullable TaskDefsProvider globalTaskDefsProvider,
            ComponentImpl component
        ) {
            this.globalResourceRegistryProvider = globalResourceRegistryProvider;
            this.globalTaskDefsProvider = globalTaskDefsProvider;
            this.component = component;
        }
    }

    private class DependencyResolver implements ComponentDependencyResolver {
        final LinkedHashMap<Coordinate, ComponentBuilder> builders;

        private DependencyResolver(ArrayList<ComponentBuilder> builders) {
            this.builders = new LinkedHashMap<>();
            for(ComponentBuilder builder : builders) {
                this.builders.put(builder.coordinate, builder);
            }
        }

        @Override public <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType) {
            final @Nullable ComponentBuilder builder = builders.get(coordinate);
            if(builder != null) {
                @SuppressWarnings("unchecked") final T subcomponent = (T)builder.subcomponents.get(subcomponentType);
                return Option.ofNullable(subcomponent);
            }
            return Option.ofNone();
        }

        @Override public <T> CollectionView<T> getSubcomponents(Class<T> subcomponentType) {
            final ArrayList<T> subcomponents = new ArrayList<>();
            for(ComponentBuilder builder : builders.values()) {
                @SuppressWarnings("unchecked") final T subcomponent = (T)builder.subcomponents.get(subcomponentType);
                if(subcomponent != null) {
                    subcomponents.add(subcomponent);
                }
            }
            return CollectionView.of(subcomponents);
        }
    }

    private static class NullResolver implements ComponentDependencyResolver {
        @Override public <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType) {
            return Option.ofNone();
        }

        @Override public <T> CollectionView<T> getSubcomponents(Class<T> subcomponentType) {
            return CollectionView.of();
        }
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private class ComponentBuilder implements SubcomponentRegistry {
        final Participant<L, R, P> participant;
        final Coordinate coordinate;
        final @Nullable String group;
        final LinkedHashMap<Class<?>, Object> subcomponents = new LinkedHashMap<>();

        @Nullable ResourceRegistriesProvider resourceRegistriesProvider;
        ResourceServiceComponent resourceServiceComponent;
        @Nullable TaskDefsProvider taskDefsProvider;
        @Nullable LanguageComponent languageComponent;
        PieComponent pieComponent;

        ComponentBuilder(Participant<L, R, P> participant) {
            this.participant = participant;
            this.coordinate = participant.getCoordinate();
            this.group = participant.getCompositionGroup();
        }

        List<ComponentBuilder> asSingleton() {
            return Collections.singletonList(this);
        }

        ComponentImpl toComponent() {
            return toComponent(false);
        }

        ComponentImpl toComponent(boolean forceNotPartOfGroup) {
            return new ComponentImpl(
                coordinate,
                !forceNotPartOfGroup && group != null,
                resourceServiceComponent,
                languageComponent,
                pieComponent,
                MapView.of(subcomponents),
                participant
            );
        }

        @Override public void register(Class<?> type, Object obj) {
            subcomponents.put(type, obj);
        }
    }


    private void addToGlobalResourceRegistryProviders(
        ComponentBuilder componentBuilder,
        ArrayList<ResourceRegistriesProvider> providers,
        DependencyResolver dependencyResolver
    ) {
        final @Nullable ResourceRegistriesProvider provider = componentBuilder.participant.getGlobalResourceRegistriesProvider(
            loggerComponent,
            baseResourceServiceComponent,
            platformComponent,
            componentBuilder,
            dependencyResolver
        );
        if(provider != null) {
            providers.add(provider);
        }
    }

    private void addToGlobalTaskDefsProvider(
        ComponentBuilder componentBuilder,
        ArrayList<TaskDefsProvider> providers,
        DependencyResolver dependencyResolver
    ) {
        final @Nullable TaskDefsProvider provider = componentBuilder.participant.getGlobalTaskDefsProvider(
            loggerComponent,
            baseResourceServiceComponent,
            componentBuilder.resourceServiceComponent,
            platformComponent,
            componentBuilder,
            dependencyResolver
        );
        if(provider != null) {
            providers.add(provider);
        }
    }

    private ResourceServiceComponent createResourceComponent(
        Iterable<ComponentBuilder> builders,
        Stream<ResourceRegistriesProvider> additionalProviders,
        Stream<Consumer<ResourceServiceModule>> customizers
    ) {
        final ResourceServiceModule resourceServiceModule = baseResourceServiceComponent.createChildModule();
        for(ComponentBuilder builder : builders) {
            if(builder.resourceRegistriesProvider != null) {
                resourceServiceModule.addRegistriesFrom(builder.resourceRegistriesProvider);
            }
        }
        additionalProviders.forEach(resourceServiceModule::addRegistriesFrom);
        for(ComponentBuilder builder : builders) {
            final @Nullable Consumer<ResourceServiceModule> customizer = builder.participant.getResourceServiceModuleCustomizer();
            if(customizer != null) {
                customizer.accept(resourceServiceModule);
            }
        }
        customizers.forEach(c -> c.accept(resourceServiceModule));
        return DaggerResourceServiceComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceModule(resourceServiceModule)
            .build();
    }

    private RootPieComponent createPieComponent(
        Iterable<ComponentBuilder> builders,
        ResourceServiceComponent resourceServiceComponent,
        Stream<TaskDefsProvider> additionalProviders,
        @Nullable ClassLoader classLoader,
        Stream<Consumer<RootPieModule>> customizers
    ) {
        // First collect into list of TaskDefsProvider such that all participants get activated.
        final ArrayList<TaskDefsProvider> taskDefsProviders = new ArrayList<>();
        for(ComponentBuilder builder : builders) {
            if(builder.taskDefsProvider != null) {
                taskDefsProviders.add(builder.taskDefsProvider);
            }
        }
        // Then add all those TaskDefsProviders to the PIE module. This order is important for the order in which
        // participants are activated in.
        final RootPieModule pieModule = new RootPieModule(pieBuilderSupplier);
        for(TaskDefsProvider taskDefsProvider : taskDefsProviders) {
            pieModule.addTaskDefsFrom(taskDefsProvider);
        }
        // Then add additional providers, set classloader, and run customizers.
        additionalProviders.forEach(pieModule::addTaskDefsFrom);
        pieModule.withSerdeFactory(loggerFactory -> new JavaSerde(classLoader));
        for(ComponentBuilder builder : builders) {
            final @Nullable Consumer<RootPieModule> customizer = builder.participant.getPieModuleCustomizer();
            if(customizer != null) {
                customizer.accept(pieModule);
            }
        }
        customizers.forEach(c -> c.accept(pieModule));
        return DaggerRootPieComponent.builder()
            .rootPieModule(pieModule)
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
    }
}
