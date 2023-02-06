package mb.spoofax.core.component;

import mb.common.util.ListView;
import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.PieComponent;
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

import java.util.function.Consumer;

public interface Participant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> extends StartedParticipant {
    /**
     * Gets the coordinate of this participant.
     */
    Coordinate getCoordinate();

    /**
     * Gets the dependencies of this participant.
     */
    ListView<CoordinateRequirement> getDependencies();

    /**
     * Gets the composition group of this participant, or {@code null} if it does not belong to a group (i.e., it is
     * standalone).
     */
    @Nullable String getCompositionGroup();

    /**
     * Gets the file extensions of the language of this participant (or an empty list if this participant has no
     * language).
     */
    ListView<String> getLanguageFileExtensions();


    /**
     * Gets the {@link LoggerComponent} class that this participant requires. Primarily used to check that the required
     * class matches when dynamically loading this participant through reflection.
     */
    default Class<? super L> getRequiredLoggerComponentClass() {
        return LoggerComponent.class;
    }

    /**
     * Gets the {@link ResourceServiceComponent} class that this participant requires. Primarily used to check that the
     * required class matches when dynamically loading this participant through reflection.
     */
    default Class<? super R> getRequiredBaseResourceServiceComponentClass() {
        return ResourceServiceComponent.class;
    }

    /**
     * Gets the {@link ResourceServiceComponent} class that this participant requires. Primarily used to check that the
     * required class matches when dynamically loading this participant through reflection.
     */
    default Class<? super P> getRequiredPlatformComponentClass() {
        return PlatformComponent.class;
    }


    /**
     * Gets a {@link ResourceRegistriesProvider resource registries provider} that should be provided globally to all
     * participants, or {@code null} if this participant does not have one. The return value of this method is only used
     * when this participant is constructed statically at startup time, not when it is dynamically loaded.
     */
    @Nullable ResourceRegistriesProvider getGlobalResourceRegistriesProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    );

    /**
     * Gets the {@link TaskDefsProvider task definitions provider} that should be provided globally to all participants,
     * or {@code null} if this participant does not have one. The return value of this method is only used when this
     * participant is constructed statically at startup time, not when it is dynamically loaded.
     */
    @Nullable TaskDefsProvider getGlobalTaskDefsProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    );


    /**
     * Gets the {@link ResourceRegistriesProvider resource registry provider} of this participant, or {@code null} if
     * this participant does not have one.
     */
    @Nullable ResourceRegistriesProvider getResourceRegistriesProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    );

    /**
     * Gets the {@link ResourcesComponent resources component} of this participant, or {@code null} if this participant
     * does not have one.
     */
    @Nullable ResourcesComponent getResourcesComponent(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    );

    /**
     * Gets a {@link ResourceServiceModule} customizer that is applied to the resource service module of this
     * participant, or {@code null} if this participant does not have one. If this participant is grouped, (i.e, {@link
     * #getCompositionGroup() returns non-null}, the customizer is applied to the module of the group.
     */
    @Nullable Consumer<ResourceServiceModule> getResourceServiceModuleCustomizer();


    /**
     * Gets the {@link TaskDefsProvider task definitions provider} of this participant, or {@code null} if this
     * participant does not have one. Only called after {@link #getResourceRegistriesProvider} has been called.
     */
    @Nullable TaskDefsProvider getTaskDefsProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    );

    /**
     * Gets the {@link LanguageComponent language component} of this participant, or {@code null} if this participant
     * does not have one. Only called after {@link #getResourceRegistriesProvider} has been called.
     */
    @Nullable LanguageComponent getLanguageComponent(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    );

    /**
     * Gets a {@link RootPieModule} customizer that is applied to the PIE module of this participant, or {@code null} if
     * this participant does not have one. If this participant is grouped, (i.e, {@link #getCompositionGroup() returns
     * non-null}, the customizer is applied to the module of the group.
     */
    @Nullable Consumer<RootPieModule> getPieModuleCustomizer();


    /**
     * Starts this participant, allowing setup code. Only called after {@link #getResourceRegistriesProvider}, {@link
     * #getTaskDefsProvider}, and {@link #getLanguageComponent} have been called.
     */
    void start(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        PieComponent pieComponent,
        ComponentDependencyResolver dependencyResolver
    );

    /**
     * Stops this participant, allowing it to free up resources.
     */
    @Override void close();
}
