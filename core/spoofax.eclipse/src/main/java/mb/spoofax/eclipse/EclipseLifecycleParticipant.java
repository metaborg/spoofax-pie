package mb.spoofax.eclipse;

import mb.pie.dagger.PieComponent;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;

public interface EclipseLifecycleParticipant extends AutoCloseable {
    ResourceRegistriesProvider getResourceRegistriesProvider();

    TaskDefsProvider getTaskDefsProvider(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    );

    void start(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent,
        PieComponent pieComponent
    );
}
