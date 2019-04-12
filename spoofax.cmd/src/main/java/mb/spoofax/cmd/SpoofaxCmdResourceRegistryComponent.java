package mb.spoofax.cmd;

import dagger.Component;
import mb.resource.string.StringResourceRegistry;
import mb.spoofax.core.platform.ResourceRegistryComponent;
import mb.spoofax.core.platform.ResourceRegistryModule;
import mb.spoofax.core.platform.ResourceRegistryScope;

@ResourceRegistryScope @Component(modules = {ResourceRegistryModule.class, SpoofaxCmdResourceRegistryModule.class})
public interface SpoofaxCmdResourceRegistryComponent extends ResourceRegistryComponent {
    StringResourceRegistry getStringResourceRegistry();
}
