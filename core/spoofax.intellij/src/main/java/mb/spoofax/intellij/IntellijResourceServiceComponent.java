package mb.spoofax.intellij;

import dagger.Component;
import mb.spoofax.core.platform.BaseResourceServiceComponent;
import mb.spoofax.core.platform.BaseResourceServiceModule;
import mb.spoofax.core.platform.ResourceServiceProviderModule;
import mb.spoofax.core.platform.ResourceServiceScope;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.spoofax.intellij.resource.IntellijResourceRegistryModule;

@ResourceServiceScope
@Component(modules = {
    ResourceServiceProviderModule.class,
    BaseResourceServiceModule.class,
    IntellijResourceRegistryModule.class
})
public interface IntellijResourceServiceComponent extends BaseResourceServiceComponent {
    IntellijResourceRegistry getResourceRegistry();
}
