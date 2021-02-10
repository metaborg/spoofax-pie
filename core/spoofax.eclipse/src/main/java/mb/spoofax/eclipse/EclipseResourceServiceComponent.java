package mb.spoofax.eclipse;

import dagger.Component;
import mb.spoofax.core.platform.BaseResourceServiceComponent;
import mb.spoofax.core.platform.BaseResourceServiceModule;
import mb.spoofax.core.platform.ResourceServiceProviderModule;
import mb.spoofax.core.platform.ResourceServiceScope;
import mb.spoofax.eclipse.resource.EclipseDocumentResourceRegistry;
import mb.spoofax.eclipse.resource.EclipseResourceRegistry;
import mb.spoofax.eclipse.resource.EclipseResourceRegistryModule;

@ResourceServiceScope
@Component(modules = {
    ResourceServiceProviderModule.class,
    BaseResourceServiceModule.class,
    EclipseResourceRegistryModule.class
})
public interface EclipseResourceServiceComponent extends BaseResourceServiceComponent {
    EclipseResourceRegistry getEclipseResourceRegistry();

    EclipseDocumentResourceRegistry getEclipseDocumentResourceRegistry();
}
