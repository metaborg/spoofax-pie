package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.fs.FSRegistry;

@Module
public class ResourceRegistryModule {
    @Provides @ResourceRegistryScope @IntoSet static ResourceRegistry provideFSRegistry() {
        return new FSRegistry();
    }
}
