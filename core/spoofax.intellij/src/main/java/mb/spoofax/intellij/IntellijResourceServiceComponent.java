package mb.spoofax.intellij;

import dagger.Component;
import mb.resource.dagger.RootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import mb.resource.dagger.ResourceServiceScope;
import mb.spoofax.intellij.log.IntellijLoggerComponent;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.spoofax.intellij.resource.IntellijResourceRegistryModule;

@ResourceServiceScope
@Component(
    modules = {
        RootResourceServiceModule.class,
        IntellijResourceRegistryModule.class
    },
    dependencies = {
        IntellijLoggerComponent.class
    }
)
public interface IntellijResourceServiceComponent extends RootResourceServiceComponent {
    IntellijResourceRegistry getResourceRegistry();
}
