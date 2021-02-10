package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;

import javax.inject.Named;
import java.util.Optional;
import java.util.Set;

@Module
public abstract class ResourceServiceProviderModule {
    @Provides @ResourceServiceScope @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static ResourceService provideResourceService(
        @Named("parent") Optional<ResourceService> parentResourceService,
        @Named("default") Optional<ResourceRegistry> defaultRegistry,
        Set<ResourceRegistry> registries
    ) {
        return parentResourceService.map(parent -> defaultRegistry
            .map(def -> parent.createChild(def, registries))
            .orElseGet(() -> parent.createChild(registries))
        ).orElseGet(() -> defaultRegistry
            .map(def -> new DefaultResourceService(def, registries))
            .orElseThrow(() -> new IllegalStateException("Cannot create resource service; default resource registry has not been set, nor has a parent resource service been set"))
        );
    }
}
