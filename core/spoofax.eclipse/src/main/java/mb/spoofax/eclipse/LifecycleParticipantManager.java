package mb.spoofax.eclipse;

import mb.common.util.MultiMap;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.SerializingStoreBuilder;
import mb.pie.runtime.store.SerializingStoreInMemoryBuffer;
import mb.pie.runtime.tracer.LoggingTracer;
import mb.resource.ResourceRegistry;
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.classloading.ParentsClassLoader;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class LifecycleParticipantManager implements AutoCloseable {
    private static class StaticGroup implements AutoCloseable {
        private final List<EclipseLifecycleParticipant> participants;
        private final ResourceServiceComponent resourceServiceComponent;
        private final PieComponent pieComponent;

        private StaticGroup(
            List<EclipseLifecycleParticipant> participants,
            ResourceServiceComponent resourceServiceComponent,
            PieComponent pieComponent
        ) {
            this.participants = participants;
            this.resourceServiceComponent = resourceServiceComponent;
            this.pieComponent = pieComponent;
        }

        public void start(EclipseLoggerComponent loggerComponent, EclipsePlatformComponent platformComponent) {
            for(EclipseLifecycleParticipant participant : participants) {
                participant.start(loggerComponent, resourceServiceComponent, platformComponent, pieComponent);
            }
        }

        @Override public void close() {
            for(EclipseLifecycleParticipant lifecycleParticipant : participants) {
                lifecycleParticipant.close();
            }
            participants.clear();
            pieComponent.close();
            resourceServiceComponent.close();
        }
    }

    public static class DynamicGroup implements AutoCloseable {
        public final ResourceRegistriesProvider resourceRegistriesProvider;
        public final ResourceServiceComponent resourceServiceComponent;
        public final LanguageComponent languageComponent;
        public final PieComponent pieComponent;
        public final SerializingStoreInMemoryBuffer serializingStoreInMemoryBuffer;
        public final EclipseLifecycleParticipant participant;

        private DynamicGroup(
            ResourceRegistriesProvider resourceRegistriesProvider,
            ResourceServiceComponent resourceServiceComponent,
            PieComponent pieComponent,
            LanguageComponent languageComponent,
            SerializingStoreInMemoryBuffer serializingStoreInMemoryBuffer,
            EclipseLifecycleParticipant participant
        ) {
            this.resourceRegistriesProvider = resourceRegistriesProvider;
            this.resourceServiceComponent = resourceServiceComponent;
            this.languageComponent = languageComponent;
            this.pieComponent = pieComponent;
            this.serializingStoreInMemoryBuffer = serializingStoreInMemoryBuffer;
            this.participant = participant;
        }


        public void start(EclipseLoggerComponent loggerComponent, EclipsePlatformComponent platformComponent) {
            participant.start(loggerComponent, resourceServiceComponent, platformComponent, pieComponent);
        }

        @Override public void close() {
            participant.close();
            pieComponent.close();
            resourceServiceComponent.close();
        }
    }

    private final EclipseLoggerComponent loggerComponent;
    private final EclipseResourceServiceComponent baseResourceServiceComponent;
    private final EclipsePlatformComponent platformComponent;

    private final HashMap<String, StaticGroup> staticGroups = new LinkedHashMap<>();
    private final HashMap<ResourcePath, DynamicGroup> dynamicGroups = new LinkedHashMap<>();


    LifecycleParticipantManager(
        EclipseLoggerComponent loggerComponent,
        EclipseResourceServiceComponent baseResourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        this.loggerComponent = loggerComponent;
        this.baseResourceServiceComponent = baseResourceServiceComponent;
        this.platformComponent = platformComponent;
    }

    @Override public void close() {
        staticGroups.values().forEach(StaticGroup::close);
        staticGroups.clear();
        dynamicGroups.values().forEach(DynamicGroup::close);
        dynamicGroups.clear();
    }


    public @Nullable LanguageComponent getLanguageComponent(String languageId) {
        for(StaticGroup group : staticGroups.values()) {
            for(EclipseLifecycleParticipant participant : group.participants) {
                final @Nullable LanguageComponent languageComponent = participant.getLanguageComponent(loggerComponent, baseResourceServiceComponent, platformComponent);
                if(languageComponent != null && languageComponent.getLanguageInstance().getId().equals(languageId)) {
                    return languageComponent;
                }
            }
        }
        for(DynamicGroup group : dynamicGroups.values()) {
            if(group.languageComponent.getLanguageInstance().getId().equals(languageId)) {
                return group.languageComponent;
            }
        }
        return null;
    }

    public @Nullable PieComponent getPieComponent(String languageId) {
        for(StaticGroup group : staticGroups.values()) {
            for(EclipseLifecycleParticipant participant : group.participants) {
                final @Nullable LanguageComponent languageComponent = participant.getLanguageComponent(loggerComponent, baseResourceServiceComponent, platformComponent);
                if(languageComponent != null && languageComponent.getLanguageInstance().getId().equals(languageId)) {
                    return group.pieComponent;
                }
            }
        }
        for(DynamicGroup group : dynamicGroups.values()) {
            if(group.languageComponent.getLanguageInstance().getId().equals(languageId)) {
                return group.pieComponent;
            }
        }
        return null;
    }


    void registerStatic(MultiMap<String, EclipseLifecycleParticipant> participantsPerGroup) {
        participantsPerGroup.forEach((groupName, participants) -> {
            final ResourceServiceComponent resourceServiceComponent = createResourceComponent(participants);
            final RootPieComponent rootPieComponent = createPieComponent(participants, resourceServiceComponent, null);
            final StaticGroup staticGroup = new StaticGroup(participants, resourceServiceComponent, rootPieComponent);
            staticGroup.start(loggerComponent, platformComponent);
            staticGroups.put(groupName, staticGroup);
        });
    }

    public DynamicGroup registerDynamic(
        ResourcePath rootDirectory,
        EclipseLifecycleParticipant participant,
        ResourceRegistry... additionalResourceRegistries
    ) {
        final @Nullable DynamicGroup previousGroup = dynamicGroups.remove(rootDirectory);
        final SerializingStoreInMemoryBuffer serializingStoreInMemoryBuffer;
        if(previousGroup != null) {
            serializingStoreInMemoryBuffer = previousGroup.serializingStoreInMemoryBuffer;
            previousGroup.close();
        } else {
            serializingStoreInMemoryBuffer = new SerializingStoreInMemoryBuffer();
        }
        final List<EclipseLifecycleParticipant> participants = Collections.singletonList(participant);
        final ResourceRegistriesProvider resourceRegistriesProvider = participant.getResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent);
        final ResourceServiceComponent resourceServiceComponent = createResourceComponent(participants, additionalResourceRegistries);
        final RootPieComponent rootPieComponent = createPieComponent(participants, resourceServiceComponent, serializingStoreInMemoryBuffer);
        final @Nullable EclipseLanguageComponent languageComponent = participant.getLanguageComponent(loggerComponent, resourceServiceComponent, platformComponent);
        if(languageComponent == null) {
            throw new RuntimeException("Cannot dynamically load language at '" + rootDirectory + "' because it returned a null EclipseLanguageComponent");
        }
        final DynamicGroup group = new DynamicGroup(resourceRegistriesProvider, resourceServiceComponent, rootPieComponent, languageComponent, serializingStoreInMemoryBuffer, participant);
        group.start(loggerComponent, platformComponent);
        dynamicGroups.put(rootDirectory, group);
        return group;
    }

    public @Nullable DynamicGroup unregisterDynamic(ResourcePath rootDirectory) {
        final @Nullable DynamicGroup previousGroup = dynamicGroups.remove(rootDirectory);
        if(previousGroup == null) return null;
        previousGroup.close();
        return previousGroup;
    }


    private ResourceServiceComponent createResourceComponent(
        Iterable<EclipseLifecycleParticipant> participants,
        ResourceRegistry... additionalResourceRegistries
    ) {
        final ResourceServiceModule resourceServiceModule = baseResourceServiceComponent.createChildModule(additionalResourceRegistries);
        for(EclipseLifecycleParticipant participant : participants) {
            resourceServiceModule.addRegistriesFrom(participant.getResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent));
        }
        return DaggerResourceServiceComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceModule(resourceServiceModule)
            .build();
    }

    private RootPieComponent createPieComponent(
        List<EclipseLifecycleParticipant> participants,
        ResourceServiceComponent resourceServiceComponent,
        @Nullable SerializingStoreInMemoryBuffer serializingStoreInMemoryBuffer
    ) {
        // First collect into list of TaskDefsProvider such that all participants get activated.
        final ArrayList<TaskDefsProvider> taskDefsProviders = new ArrayList<>();
        for(EclipseLifecycleParticipant participant : participants) {
            final TaskDefsProvider taskDefsProvider = participant.getTaskDefsProvider(loggerComponent, resourceServiceComponent, platformComponent);
            taskDefsProviders.add(taskDefsProvider);
        }
        // Then add all those TaskDefsProviders to the PIE module. This order is important for the order in which
        // participants are activated in eclipse.
        final RootPieModule pieModule = new RootPieModule(PieBuilderImpl::new);
        for(TaskDefsProvider taskDefsProvider : taskDefsProviders) {
            pieModule.addTaskDefsFrom(taskDefsProvider);
        }
        if(serializingStoreInMemoryBuffer != null) {
            final @Nullable ClassLoader deserializeClassLoader;
            if(participants.size() == 1) {
                deserializeClassLoader = participants.get(0).getClass().getClassLoader();
            } else if(participants.size() != 0) {
                deserializeClassLoader = new ParentsClassLoader(participants.stream().map(p -> p.getClass().getClassLoader()).collect(Collectors.toList()));
            } else {
                deserializeClassLoader = null;
            }
            pieModule.withStoreFactory((serde, resourceService, loggerFactory) -> SerializingStoreBuilder.ofInMemoryStore(serde)
                .withDeserializeClassLoader(deserializeClassLoader)
                .withInMemoryBuffer(serializingStoreInMemoryBuffer)
                .withLoggingDeserializeFailHandler(loggerFactory)
                .build()
            );
        }
        participants.forEach(p -> p.customizePieModule(pieModule));
        // HACK: enable logging for all dynamic PIE instances
        pieModule.withTracerFactory(LoggingTracer::new);
        return DaggerRootPieComponent.builder()
            .rootPieModule(pieModule)
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
    }
}
