package mb.spoofax.core.platform;

import dagger.Component;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;

import java.util.Set;

@ResourceServiceScope
@Component(modules = {
    ResourceServiceProviderModule.class,
    ResourceServiceModule.class
})
public interface ResourceServiceComponent {
    ResourceService getResourceService();


    default ResourceServiceModule createChildModule() {
        return new ResourceServiceModule(getResourceService());
    }

    default ResourceServiceModule createChildModule(Set<ResourceRegistry> registries) {
        return new ResourceServiceModule(getResourceService(), registries);
    }

    default ResourceServiceModule createChildModule(ResourceRegistry... registries) {
        return new ResourceServiceModule(getResourceService(), registries);
    }

    default ResourceServiceModule createChildModuleWithDefault(ResourceRegistry defaultRegistry, Set<ResourceRegistry> registries) {
        return new ResourceServiceModule(getResourceService(), defaultRegistry, registries);
    }

    default ResourceServiceModule createChildModuleWithDefault(ResourceRegistry defaultRegistry, ResourceRegistry... registries) {
        return new ResourceServiceModule(getResourceService(), defaultRegistry, registries);
    }
}
