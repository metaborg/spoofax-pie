package mb.spoofax.eclipse;

import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface EclipseLifecycleParticipant extends AutoCloseable {
    ResourceRegistriesProvider getResourceRegistriesProvider(
        EclipseLoggerComponent loggerComponent,
        EclipseResourceServiceComponent baseResourceServiceComponent,
        EclipsePlatformComponent platformComponent
    );

    TaskDefsProvider getTaskDefsProvider(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    );

    @Nullable EclipseLanguageComponent getLanguageComponent(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    );

    default void customizePieModule(RootPieModule pieModule) {}

    void start(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent,
        PieComponent pieComponent
    );

    @Override void close();
}
