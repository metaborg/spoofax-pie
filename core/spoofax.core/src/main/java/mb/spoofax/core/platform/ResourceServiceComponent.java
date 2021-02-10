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


    default ResourceServiceModule createParentModule() {
        return new ResourceServiceModule(getResourceService());
    }

    default ResourceServiceModule createParentModule(Set<ResourceRegistry> registries) {
        return new ResourceServiceModule(getResourceService(), registries);
    }

    default ResourceServiceModule createParentModule(ResourceRegistry... registries) {
        return new ResourceServiceModule(getResourceService(), registries);
    }

    default ResourceServiceModule createParentModuleWithDefault(ResourceRegistry defaultRegistry, Set<ResourceRegistry> registries) {
        return new ResourceServiceModule(getResourceService(), defaultRegistry, registries);
    }

    default ResourceServiceModule createParentModuleWithDefault(ResourceRegistry defaultRegistry, ResourceRegistry... registries) {
        return new ResourceServiceModule(getResourceService(), defaultRegistry, registries);
    }
}
