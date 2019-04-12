package mb.spoofax.core.platform;

import dagger.Component;
import mb.resource.ResourceRegistry;

import java.util.Set;

@ResourceRegistryScope @Component(modules = {ResourceRegistryModule.class})
public interface ResourceRegistryComponent {
    Set<ResourceRegistry> getResourceRegistries();
}
