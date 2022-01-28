package mb.spoofax.core.component;

import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.PieComponent;
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

public interface Participant extends AutoCloseable {
    /**
     * Gets the coordinates of this participant.
     */
    Coordinate getCoordinates();

    /**
     * Gets the group of this participant, or {@code null} if it does not belong to a group (i.e., it is standalone).
     */
    @Nullable String getGroup();


    /**
     * Gets a {@link ResourceRegistriesProvider resource registries provider} that should be provided globally to all
     * participants, or {@code null} if this participant does not have one. The return value of this method is only used
     * when this participant is constructed statically at startup time, not when it is dynamically loaded.
     */
    default @Nullable ResourceRegistriesProvider getGlobalResourceRegistriesProvider(
        LoggerComponent loggerComponent,
        ResourceServiceComponent baseResourceServiceComponent,
        PlatformComponent platformComponent
    ) {return null;}

    /**
     * Gets the {@link TaskDefsProvider task definitions provider} that should be provided globally to all participants,
     * or {@code null} if this participant does not have one. The return value of this method is only used when this
     * participant is constructed statically at startup time, not when it is dynamically loaded.
     */
    default @Nullable TaskDefsProvider getGlobalTaskDefsProvider(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent
    ) {return null;}


    /**
     * Gets the {@link ResourceRegistriesProvider resource registries provider} of this participant, or {@code null} if
     * this participant does not have one.
     */
    @Nullable ResourceRegistriesProvider getResourceRegistriesProvider(
        LoggerComponent loggerComponent,
        ResourceServiceComponent baseResourceServiceComponent,
        PlatformComponent platformComponent
    );

    /**
     * Gets a {@link ResourceServiceModule} customizer that is applied to the resource service module of this
     * participant, or {@code null} if this participant does not have one. If this participant is grouped, (i.e, {@link
     * #getGroup() returns non-null}, the customizer is applied to the module of the group.
     */
    @Nullable default Consumer<ResourceServiceModule> getResourceServiceModuleCustomizer() {return null;}

    /**
     * Gets the {@link TaskDefsProvider task definitions provider} of this participant, or {@code null} if this
     * participant does not have one. Only called after {@link #getResourceRegistriesProvider} has been called.
     */
    @Nullable TaskDefsProvider getTaskDefsProvider(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent
    );

    /**
     * Gets a {@link RootPieModule} customizer that is applied to the PIE module of this participant, or {@code null} if
     * this participant does not have one. If this participant is grouped, (i.e, {@link #getGroup() returns non-null},
     * the customizer is applied to the module of the group.
     */
    @Nullable default Consumer<RootPieModule> getPieModuleCustomizer() {return null;}


    /**
     * Gets the {@link LanguageComponent language component} of this participant, or {@code null} if this participant
     * does not have one. Only called after {@link #getResourceRegistriesProvider} and {@link #getTaskDefsProvider} have
     * been called.
     */
    @Nullable LanguageComponent getLanguageComponent(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent
    );


    /**
     * Starts this participant, allowing setup code. Only called after {@link #getResourceRegistriesProvider}, {@link
     * #getTaskDefsProvider}, and {@link #getLanguageComponent} have been called.
     */
    void start(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,
        PieComponent pieComponent
    );

    /**
     * Stops this participant, allowing it to free up resources.
     */
    @Override void close();
}
