package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;

import javax.inject.Singleton;
import java.util.Set;

@Module
public class ResourceServiceModule {
    @Provides @Singleton static ResourceService provideResourceRegistry(Set<ResourceRegistry> registries) {
        if(registries.isEmpty()) {
            throw new RuntimeException("Cannot provide resource service; no resource registries have been set");
        }
        return new DefaultResourceService(registries);
    }
}
