package mb.spoofax.eclipse;

import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;

public interface EclipseLanguage extends AutoCloseable {
    ResourceRegistriesProvider createResourcesComponent();

    EclipseLanguageComponent createComponent(
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
