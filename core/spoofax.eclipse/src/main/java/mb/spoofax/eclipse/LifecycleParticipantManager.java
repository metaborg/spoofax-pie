package mb.spoofax.eclipse;

import mb.common.util.MultiMap;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
        public final EclipseLifecycleParticipant participant;

        private DynamicGroup(
            ResourceRegistriesProvider resourceRegistriesProvider,
            ResourceServiceComponent resourceServiceComponent,
            PieComponent pieComponent,
            LanguageComponent languageComponent,
            EclipseLifecycleParticipant participant
        ) {
            this.resourceRegistriesProvider = resourceRegistriesProvider;
            this.resourceServiceComponent = resourceServiceComponent;
            this.languageComponent = languageComponent;
            this.pieComponent = pieComponent;
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


    void registerStatic(MultiMap<String, EclipseLifecycleParticipant> participantsPerGroup) {
        participantsPerGroup.forEach((groupName, participants) -> {
            final ResourceServiceComponent resourceServiceComponent = createResourceComponent(participants);
            final RootPieComponent rootPieComponent = createPieComponent(participants, resourceServiceComponent);
            final StaticGroup staticGroup = new StaticGroup(participants, resourceServiceComponent, rootPieComponent);
            staticGroup.start(loggerComponent, platformComponent);
            staticGroups.put(groupName, staticGroup);
        });
    }

    public DynamicGroup registerDynamic(ResourcePath rootDirectory, EclipseLifecycleParticipant participant) {
        final @Nullable DynamicGroup previousGroup = dynamicGroups.remove(rootDirectory);
        if(previousGroup != null) {
            previousGroup.close();
        }
        final List<EclipseLifecycleParticipant> participants = Collections.singletonList(participant);
        final ResourceRegistriesProvider resourceRegistriesProvider = participant.getResourceRegistriesProvider(loggerComponent);
        final ResourceServiceComponent resourceServiceComponent = createResourceComponent(participants);
        final RootPieComponent rootPieComponent = createPieComponent(participants, resourceServiceComponent);
        final @Nullable EclipseLanguageComponent languageComponent = participant.getLanguageComponent(loggerComponent, resourceServiceComponent, platformComponent);
        if(languageComponent == null) {
            throw new RuntimeException("Cannot dynamically load language at '" + rootDirectory + "' because it returned a null EclipseLanguageComponent");
        }
        final DynamicGroup group = new DynamicGroup(resourceRegistriesProvider, resourceServiceComponent, rootPieComponent, languageComponent, participant);
        group.start(loggerComponent, platformComponent);
        dynamicGroups.put(rootDirectory, group);
        return group;
    }


    private ResourceServiceComponent createResourceComponent(
        Iterable<EclipseLifecycleParticipant> participants
    ) {
        final ResourceServiceModule resourceServiceModule = baseResourceServiceComponent.createChildModule();
        for(EclipseLifecycleParticipant participant : participants) {
            resourceServiceModule.addRegistriesFrom(participant.getResourceRegistriesProvider(loggerComponent));
        }
        return DaggerResourceServiceComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceModule(resourceServiceModule)
            .build();
    }

    private RootPieComponent createPieComponent(
        Iterable<EclipseLifecycleParticipant> participants,
        ResourceServiceComponent resourceServiceComponent
    ) {
        final ArrayList<TaskDefsProvider> taskDefsProviders = new ArrayList<>();
        for(EclipseLifecycleParticipant participant : participants) {
            final TaskDefsProvider taskDefsProvider = participant.getTaskDefsProvider(loggerComponent, resourceServiceComponent, platformComponent);
            taskDefsProviders.add(taskDefsProvider);
        }
        final RootPieModule pieModule = new RootPieModule(PieBuilderImpl::new);
        for(TaskDefsProvider taskDefsProvider : taskDefsProviders) {
            pieModule.addTaskDefsFrom(taskDefsProvider);
        }
        participants.forEach(p -> p.customizePieModule(pieModule));
        return DaggerRootPieComponent.builder()
            .rootPieModule(pieModule)
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
    }
}
