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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ComponentBuilder {
    protected final LoggerComponent loggerComponent;
    protected final ResourceServiceComponent baseResourceServiceComponent;
    protected final PlatformComponent platformComponent;
    protected final Supplier<PieBuilder> pieBuilderSupplier;


    protected ComponentBuilder(
        LoggerComponent loggerComponent,
        ResourceServiceComponent baseResourceServiceComponent,
        PlatformComponent platformComponent,
        Supplier<PieBuilder> pieBuilderSupplier
    ) {
        this.loggerComponent = loggerComponent;
        this.baseResourceServiceComponent = baseResourceServiceComponent;
        this.platformComponent = platformComponent;
        this.pieBuilderSupplier = pieBuilderSupplier;
    }


    /**
     * Builds {@link StandaloneComponent}s and {@link GroupedComponents} from {@link Participant}s.
     */
    protected BuildResult build(
        Iterable<Participant> participants,
        ListView<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers,
        MultiMapView<String, Consumer<ResourceServiceModule>> groupedResourceServiceModuleCustomizers,
        ListView<Consumer<RootPieModule>> pieModuleCustomizers,
        MultiMapView<String, Consumer<RootPieModule>> groupedPieModuleCustomizers,
        @Nullable ClassLoader classLoader
    ) {
        // Create builders for participants.
        final ArrayList<StandaloneBuilder> standaloneBuilders = new ArrayList<>();
        final ArrayList<GroupBuilder> groupBuilders = new ArrayList<>();
        {
            final MultiMap<String, Participant> groupedParticipants = MultiMap.withLinkedHash();
            participants.forEach(p -> {
                final @Nullable String group = p.getGroup();
                if(group != null) {
                    groupedParticipants.put(group, p);
                } else {
                    standaloneBuilders.add(new StandaloneBuilder(p));
                }
            });
            groupedParticipants.forEach((g, ps) -> groupBuilders.add(new GroupBuilder(ps, g)));
        }

        // Gather global resource registry providers.
        final ArrayList<ResourceRegistriesProvider> globalResourceRegistryProviders = new ArrayList<>();
        standaloneBuilders.forEach(b -> addToGlobalResourceRegistryProviders(b.participant, globalResourceRegistryProviders));
        groupBuilders.forEach(b -> b.participants.forEach(p -> addToGlobalResourceRegistryProviders(p, globalResourceRegistryProviders)));

        // Create resource service components.
        standaloneBuilders.forEach(b -> b.resourceServiceComponent = createResourceComponent(
            b.singletonParticipant(),
            globalResourceRegistryProviders,
            resourceServiceModuleCustomizers.stream()
        ));
        groupBuilders.forEach(b -> b.resourceServiceComponent = createResourceComponent(
            participants,
            globalResourceRegistryProviders,
            Stream.concat(resourceServiceModuleCustomizers.stream(), groupedResourceServiceModuleCustomizers.get(b.group).stream())
        ));

        // Gather global task definition providers.
        final ArrayList<TaskDefsProvider> globalTaskDefsProviders = new ArrayList<>();
        standaloneBuilders.forEach(b -> addToGlobalTaskDefsProvider(b.participant, globalTaskDefsProviders, b.resourceServiceComponent));
        groupBuilders.forEach(b -> {
            for(Participant participant : b.participants) {
                addToGlobalTaskDefsProvider(participant, globalTaskDefsProviders, b.resourceServiceComponent);
            }
        });

        // Create language components.
        standaloneBuilders.forEach(b -> b.languageComponent = b.participant.getLanguageComponent(loggerComponent, b.resourceServiceComponent, platformComponent));
        groupBuilders.forEach(b -> {
            for(Participant participant : b.participants) {
                final @Nullable LanguageComponent languageComponent = participant.getLanguageComponent(loggerComponent, b.resourceServiceComponent, platformComponent);
                if(languageComponent != null) {
                    b.languageComponents.put(participant.getCoordinates(), languageComponent);
                }
            }
        });

        // Create PIE modules.
        standaloneBuilders.forEach(b -> b.pieComponent = createPieComponent(
            b.singletonParticipant(),
            b.resourceServiceComponent,
            globalTaskDefsProviders,
            classLoader,
            pieModuleCustomizers.stream()
        ));
        groupBuilders.forEach(b -> b.pieComponent = createPieComponent(
            b.participants,
            b.resourceServiceComponent,
            globalTaskDefsProviders,
            classLoader,
            Stream.concat(pieModuleCustomizers.stream(), groupedPieModuleCustomizers.get(b.group).stream())
        ));

        // Start all components.
        standaloneBuilders.forEach(b -> b.participant.start(loggerComponent, b.resourceServiceComponent, platformComponent, b.pieComponent));
        groupBuilders.forEach(b -> {
            for(Participant participant : b.participants) {
                participant.start(loggerComponent, b.resourceServiceComponent, platformComponent, b.pieComponent);
            }
        });

        return new BuildResult(
            ListView.of(globalResourceRegistryProviders),
            ListView.of(globalTaskDefsProviders),
            ListView.<StandaloneComponent>of(standaloneBuilders.stream().map(StaticComponentManagerBuilder.StandaloneBuilder::build).collect(Collectors.toCollection(ArrayList::new))),
            MapView.of((Map<String, GroupedComponents>)groupBuilders.stream().map(GroupBuilder::build).collect(Collectors.toMap(g -> g.group, g -> g, (x, y) -> y, LinkedHashMap::new)))
        );
    }

    static protected class BuildResult {
        public final ListView<ResourceRegistriesProvider> globalResourceRegistryProviders;
        public final ListView<TaskDefsProvider> globalTaskDefsProviders;
        public final ListView<StandaloneComponent> standaloneComponents;
        public final MapView<String, GroupedComponents> groupedComponents;

        private BuildResult(
            ListView<ResourceRegistriesProvider> globalResourceRegistryProviders,
            ListView<TaskDefsProvider> globalTaskDefsProviders,
            ListView<StandaloneComponent> standaloneComponents,
            MapView<String, GroupedComponents> groupedComponents
        ) {
            this.globalResourceRegistryProviders = globalResourceRegistryProviders;
            this.globalTaskDefsProviders = globalTaskDefsProviders;
            this.standaloneComponents = standaloneComponents;
            this.groupedComponents = groupedComponents;
        }
    }


    /**
     * Builds one {@link StandaloneComponent} from one {@link Participant}. The group of the participant is ignored.
     */
    protected BuildOneResult buildOne(
        Participant participant,
        Iterable<ResourceRegistriesProvider> additionalResourceRegistriesProviders,
        ListView<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers,
        Iterable<TaskDefsProvider> additionalTaskDefsProviders,
        ListView<Consumer<RootPieModule>> pieModuleCustomizers,
        @Nullable ClassLoader classLoader
    ) {
        final StandaloneBuilder builder = new StandaloneBuilder(participant);
        builder.resourceServiceComponent = createResourceComponent(
            builder.singletonParticipant(),
            additionalResourceRegistriesProviders,
            resourceServiceModuleCustomizers.stream()
        );
        builder.languageComponent = participant.getLanguageComponent(loggerComponent, builder.resourceServiceComponent, platformComponent);
        builder.pieComponent = createPieComponent(
            builder.singletonParticipant(),
            builder.resourceServiceComponent,
            additionalTaskDefsProviders,
            classLoader,
            pieModuleCustomizers.stream()
        );
        participant.start(loggerComponent, builder.resourceServiceComponent, platformComponent, builder.pieComponent);
        return new BuildOneResult(
            participant.getGlobalResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent),
            participant.getGlobalTaskDefsProvider(loggerComponent, baseResourceServiceComponent, platformComponent),
            builder.build()
        );
    }

    static protected class BuildOneResult {
        public final @Nullable ResourceRegistriesProvider globalResourceRegistryProvider;
        public final @Nullable TaskDefsProvider globalTaskDefsProvider;
        public final StandaloneComponent component;

        public BuildOneResult(
            @Nullable ResourceRegistriesProvider globalResourceRegistryProvider,
            @Nullable TaskDefsProvider globalTaskDefsProvider,
            StandaloneComponent component
        ) {
            this.globalResourceRegistryProvider = globalResourceRegistryProvider;
            this.globalTaskDefsProvider = globalTaskDefsProvider;
            this.component = component;
        }
    }


    @SuppressWarnings("NotNullFieldNotInitialized")
    private static class StandaloneBuilder {
        public final Participant participant;
        public ResourceServiceComponent resourceServiceComponent;
        public @Nullable LanguageComponent languageComponent;
        public PieComponent pieComponent;

        public StandaloneBuilder(Participant participant) {
            this.participant = participant;
        }

        public List<Participant> singletonParticipant() {
            return Collections.singletonList(participant);
        }

        public StandaloneComponent build() {
            return new StandaloneComponent(participant, resourceServiceComponent, languageComponent, pieComponent);
        }
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private static class GroupBuilder {
        public final ArrayList<Participant> participants;
        public final String group;
        public ResourceServiceComponent resourceServiceComponent;
        public final LinkedHashMap<Coordinate, LanguageComponent> languageComponents = new LinkedHashMap<>();
        public PieComponent pieComponent;

        public GroupBuilder(ArrayList<Participant> participants, String group) {
            this.group = group;
            this.participants = participants;
        }

        public GroupedComponents build() {
            return new GroupedComponents(ListView.of(participants), group, resourceServiceComponent, MapView.of(languageComponents), pieComponent);
        }
    }

    private void addToGlobalResourceRegistryProviders(Participant participant, ArrayList<ResourceRegistriesProvider> providers) {
        final @Nullable ResourceRegistriesProvider provider = participant.getGlobalResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent);
        if(provider != null) {
            providers.add(provider);
        }
    }

    private void addToGlobalTaskDefsProvider(Participant participant, ArrayList<TaskDefsProvider> providers, ResourceServiceComponent resourceServiceComponent) {
        final @Nullable TaskDefsProvider provider = participant.getGlobalTaskDefsProvider(loggerComponent, resourceServiceComponent, platformComponent);
        if(provider != null) {
            providers.add(provider);
        }
    }

    private ResourceServiceComponent createResourceComponent(
        Iterable<Participant> participants,
        Iterable<ResourceRegistriesProvider> additionalProviders,
        Stream<Consumer<ResourceServiceModule>> customizers
    ) {
        final ResourceServiceModule resourceServiceModule = baseResourceServiceComponent.createChildModule();
        for(Participant participant : participants) {
            final @Nullable ResourceRegistriesProvider provider = participant.getResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent);
            if(provider != null) {
                resourceServiceModule.addRegistriesFrom(provider);
            }
        }
        additionalProviders.forEach(resourceServiceModule::addRegistriesFrom);
        for(Participant participant : participants) {
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
        Iterable<Participant> participants,
        ResourceServiceComponent resourceServiceComponent,
        Iterable<TaskDefsProvider> additionalProviders,
        @Nullable ClassLoader classLoader,
        Stream<Consumer<RootPieModule>> customizers
    ) {
        // First collect into list of TaskDefsProvider such that all participants get activated.
        final ArrayList<TaskDefsProvider> taskDefsProviders = new ArrayList<>();
        for(Participant participant : participants) {
            final @Nullable TaskDefsProvider taskDefsProvider = participant.getTaskDefsProvider(loggerComponent, resourceServiceComponent, platformComponent);
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
        for(Participant participant : participants) {
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
