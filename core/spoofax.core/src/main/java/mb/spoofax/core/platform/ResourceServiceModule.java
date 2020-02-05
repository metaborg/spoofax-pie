package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Set;

@Module
public class ResourceServiceModule {
    @Provides @Singleton
    static ResourceService provideResourceRegistry(@Named("default") ResourceRegistry defaultRegistry, Set<ResourceRegistry> registries) {
        return new DefaultResourceService(defaultRegistry, registries);
    }
}
