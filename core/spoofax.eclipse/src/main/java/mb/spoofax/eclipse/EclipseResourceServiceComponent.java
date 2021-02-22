package mb.spoofax.eclipse;

import dagger.Component;
import mb.resource.dagger.RootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import mb.resource.dagger.ResourceServiceScope;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.eclipse.resource.EclipseDocumentResourceRegistry;
import mb.spoofax.eclipse.resource.EclipseResourceRegistry;
import mb.spoofax.eclipse.resource.EclipseResourceRegistryModule;

@ResourceServiceScope
@Component(
    modules = {
        RootResourceServiceModule.class,
        EclipseResourceRegistryModule.class
    },
    dependencies = {
        EclipseLoggerComponent.class
    }
)
public interface EclipseResourceServiceComponent extends RootResourceServiceComponent {
    EclipseResourceRegistry getEclipseResourceRegistry();

    EclipseDocumentResourceRegistry getEclipseDocumentResourceRegistry();
}
