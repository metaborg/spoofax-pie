package mb.spt.resource;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.spt.SptResourcesScope;

@Module
public abstract class SptTestCaseResourceModule {
    @Provides @SptResourcesScope
    static SptTestCaseResourceRegistry provideTestCaseResourceRegistry() {
        return new SptTestCaseResourceRegistry();
    }

    @Provides @SptResourcesScope @IntoSet
    static ResourceRegistry provideClassLoaderResourceRegistryIntoSet(SptTestCaseResourceRegistry registry) {
        return registry;
    }
}
