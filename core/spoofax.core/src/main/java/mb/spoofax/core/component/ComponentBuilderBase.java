package mb.spoofax.core.component;

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
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
        // TODO: check for components with duplicate coordinates?

        // Create builders for participants.
        final ArrayList<ComponentBuilder> componentBuilders = new ArrayList<>();
        final ArrayList<ComponentGroupBuilder> componentGroupBuilders = new ArrayList<>();
        {
            final MultiMap<String, Participant<L, R, P>> groupedParticipants = MultiMap.withLinkedHash();
            participants.forEach(p -> {
                final @Nullable String group = p.getGroup();
                if(group != null) {
                    groupedParticipants.put(group, p);
                } else {
                    componentBuilders.add(new ComponentBuilder(p));
                }
            });
            groupedParticipants.forEach((g, ps) -> componentGroupBuilders.add(new ComponentGroupBuilder(ps, g)));
        }

        // Gather global resource registry providers.
        final ArrayList<ResourceRegistriesProvider> globalResourceRegistryProviders = new ArrayList<>();
        componentBuilders.forEach(b -> addToGlobalResourceRegistryProviders(b.participant, globalResourceRegistryProviders));
        componentGroupBuilders.forEach(b -> b.participants.forEach(p -> addToGlobalResourceRegistryProviders(p, globalResourceRegistryProviders)));

        // Create resource service components.
        componentBuilders.forEach(b -> b.resourceServiceComponent = createResourceComponent(
            b.singletonParticipant(),
            globalResourceRegistryProviders,
            resourceServiceModuleCustomizers.stream()
        ));
        componentGroupBuilders.forEach(b -> {
            final ResourceServiceComponent resourceServiceComponent = createResourceComponent(
                participants,
                globalResourceRegistryProviders,
                Stream.concat(resourceServiceModuleCustomizers.stream(), groupedResourceServiceModuleCustomizers.get(b.group).stream())
            );
            b.resourceServiceComponent = resourceServiceComponent;
            b.componentBuilders.values().forEach(cb -> cb.resourceServiceComponent = resourceServiceComponent);
        });

        // Gather global task definition providers.
        final ArrayList<TaskDefsProvider> globalTaskDefsProviders = new ArrayList<>();
        componentBuilders.forEach(b -> addToGlobalTaskDefsProvider(b.participant, globalTaskDefsProviders, b.resourceServiceComponent));
        componentGroupBuilders.forEach(b -> {
            for(Participant<L, R, P> participant : b.participants) {
                addToGlobalTaskDefsProvider(participant, globalTaskDefsProviders, b.resourceServiceComponent);
            }
        });

        // Create language components.
        componentBuilders.forEach(b -> b.languageComponent = b.participant.getLanguageComponent(
            loggerComponent,
            baseResourceServiceComponent,
            b.resourceServiceComponent,
            platformComponent
        ));
        componentGroupBuilders.forEach(b -> {
            for(ComponentBuilder componentBuilder : b.componentBuilders.values()) {
                componentBuilder.languageComponent = componentBuilder.participant.getLanguageComponent(
                    loggerComponent,
                    baseResourceServiceComponent,
                    b.resourceServiceComponent,
                    platformComponent
                );
            }
        });

        // Create PIE modules.
        componentBuilders.forEach(b -> b.pieComponent = createPieComponent(
            b.singletonParticipant(),
            b.resourceServiceComponent,
            globalTaskDefsProviders,
            classLoader,
            pieModuleCustomizers.stream()
        ));
        componentGroupBuilders.forEach(b -> {
            final PieComponent pieComponent = createPieComponent(
                b.participants,
                b.resourceServiceComponent,
                globalTaskDefsProviders,
                classLoader,
                Stream.concat(pieModuleCustomizers.stream(), groupedPieModuleCustomizers.get(b.group).stream())
            );
            b.pieComponent = pieComponent;
            b.componentBuilders.values().forEach(cb -> cb.pieComponent = pieComponent);
        });

        // Create subcomponents.
        componentBuilders.forEach(b -> b.subcomponents = b.participant.getSubcomponents(
            loggerComponent,
            baseResourceServiceComponent,
            b.resourceServiceComponent,
            platformComponent,
            b.pieComponent
        ));
        componentGroupBuilders.forEach(b -> {
            for(ComponentBuilder componentBuilder : b.componentBuilders.values()) {
                componentBuilder.subcomponents = componentBuilder.participant.getSubcomponents(
                    loggerComponent,
                    baseResourceServiceComponent,
                    b.resourceServiceComponent,
                    platformComponent,
                    b.pieComponent
                );
            }
        });

        // Start all components.
        componentBuilders.forEach(b -> b.participant.start(loggerComponent, baseResourceServiceComponent, b.resourceServiceComponent, platformComponent, b.pieComponent));
        componentGroupBuilders.forEach(b -> {
            for(Participant<L, R, P> participant : b.participants) {
                participant.start(loggerComponent, baseResourceServiceComponent, b.resourceServiceComponent, platformComponent, b.pieComponent);
            }
        });

        // Gather components.
        final ArrayList<ComponentImpl> components = new ArrayList<>();
        final ArrayList<ComponentGroupImpl> componentGroups = new ArrayList<>();
        for(ComponentBuilder builder : componentBuilders) {
            components.add(builder.toComponent(false));
        }
        for(ComponentGroupBuilder builder : componentGroupBuilders) {
            final ComponentGroupImpl componentGroup = builder.toComponentGroup();
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
        Iterable<ResourceRegistriesProvider> additionalResourceRegistriesProviders,
        ListView<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers,
        Iterable<TaskDefsProvider> additionalTaskDefsProviders,
        ListView<Consumer<RootPieModule>> pieModuleCustomizers,
        @Nullable ClassLoader classLoader
    ) {
        final ComponentBuilder builder = new ComponentBuilder(participant);
        builder.resourceServiceComponent = createResourceComponent(
            builder.singletonParticipant(),
            additionalResourceRegistriesProviders,
            resourceServiceModuleCustomizers.stream()
        );
        builder.languageComponent = participant.getLanguageComponent(loggerComponent, baseResourceServiceComponent, builder.resourceServiceComponent, platformComponent);
        builder.pieComponent = createPieComponent(
            builder.singletonParticipant(),
            builder.resourceServiceComponent,
            additionalTaskDefsProviders,
            classLoader,
            pieModuleCustomizers.stream()
        );
        participant.start(loggerComponent, baseResourceServiceComponent, builder.resourceServiceComponent, platformComponent, builder.pieComponent);
        return new BuildOneResult(
            participant.getGlobalResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent),
            participant.getGlobalTaskDefsProvider(loggerComponent, baseResourceServiceComponent, platformComponent),
            builder.toComponent(false)
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


    @SuppressWarnings("NotNullFieldNotInitialized")
    private class ComponentBuilder {
        final Participant<L, R, P> participant;
        final Coordinate coordinate;

        ResourceServiceComponent resourceServiceComponent;
        @Nullable LanguageComponent languageComponent;
        PieComponent pieComponent;
        MapView<Class<?>, Object> subcomponents;

        ComponentBuilder(Participant<L, R, P> participant) {
            this.participant = participant;
            this.coordinate = participant.getCoordinate();
        }

        List<Participant<L, R, P>> singletonParticipant() {
            return Collections.singletonList(participant);
        }

        ComponentImpl toComponent(boolean partOfGroup) {
            return new ComponentImpl(coordinate, partOfGroup, resourceServiceComponent, languageComponent, pieComponent, subcomponents, participant);
        }
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private class ComponentGroupBuilder {
        final ArrayList<Participant<L, R, P>> participants;
        final String group;
        final LinkedHashMap<Coordinate, ComponentBuilder> componentBuilders;

        ResourceServiceComponent resourceServiceComponent;
        PieComponent pieComponent;

        ComponentGroupBuilder(ArrayList<Participant<L, R, P>> participants, String group) {
            this.group = group;
            this.participants = participants;
            this.componentBuilders = new LinkedHashMap<>();
            for(Participant<L, R, P> participant : participants) {
                componentBuilders.put(participant.getCoordinate(), new ComponentBuilder(participant));
            }
        }

        ComponentGroupImpl toComponentGroup() {
            final LinkedHashMap<Coordinate, ComponentImpl> components = new LinkedHashMap<>();
            for(Map.Entry<Coordinate, ComponentBuilder> entry : componentBuilders.entrySet()) {
                components.put(entry.getKey(), entry.getValue().toComponent(true));
            }
            return new ComponentGroupImpl(group, resourceServiceComponent, pieComponent, MapView.of(components));
        }
    }

    private void addToGlobalResourceRegistryProviders(Participant<L, R, P> participant, ArrayList<ResourceRegistriesProvider> providers) {
        final @Nullable ResourceRegistriesProvider provider = participant.getGlobalResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent);
        if(provider != null) {
            providers.add(provider);
        }
    }

    private void addToGlobalTaskDefsProvider(Participant<L, R, P> participant, ArrayList<TaskDefsProvider> providers, ResourceServiceComponent resourceServiceComponent) {
        final @Nullable TaskDefsProvider provider = participant.getGlobalTaskDefsProvider(loggerComponent, resourceServiceComponent, platformComponent);
        if(provider != null) {
            providers.add(provider);
        }
    }

    private ResourceServiceComponent createResourceComponent(
        Iterable<Participant<L, R, P>> participants,
        Iterable<ResourceRegistriesProvider> additionalProviders,
        Stream<Consumer<ResourceServiceModule>> customizers
    ) {
        final ResourceServiceModule resourceServiceModule = baseResourceServiceComponent.createChildModule();
        for(Participant<L, R, P> participant : participants) {
            final @Nullable ResourceRegistriesProvider provider = participant.getResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent);
            if(provider != null) {
                resourceServiceModule.addRegistriesFrom(provider);
            }
        }
        additionalProviders.forEach(resourceServiceModule::addRegistriesFrom);
        for(Participant<L, R, P> participant : participants) {
            final @Nullable Consumer<ResourceServiceModule> customizer = participant.getResourceServiceModuleCustomizer();
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
        Iterable<Participant<L, R, P>> participants,
        ResourceServiceComponent resourceServiceComponent,
        Iterable<TaskDefsProvider> additionalProviders,
        @Nullable ClassLoader classLoader,
        Stream<Consumer<RootPieModule>> customizers
    ) {
        // First collect into list of TaskDefsProvider such that all participants get activated.
        final ArrayList<TaskDefsProvider> taskDefsProviders = new ArrayList<>();
        for(Participant<L, R, P> participant : participants) {
            final @Nullable TaskDefsProvider taskDefsProvider = participant.getTaskDefsProvider(loggerComponent, baseResourceServiceComponent, resourceServiceComponent, platformComponent);
            if(taskDefsProvider != null) {
                taskDefsProviders.add(taskDefsProvider);
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
        for(Participant<L, R, P> participant : participants) {
            final @Nullable Consumer<RootPieModule> customizer = participant.getPieModuleCustomizer();
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
