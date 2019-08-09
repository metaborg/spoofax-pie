package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.fs.FSRegistry;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class FSRegistryModule {
    private final FSRegistry registry = new FSRegistry();

    @Provides @Singleton @IntoSet ResourceRegistry provideIntoSet() {
        return registry;
    }

    @Provides @Named("default") @Singleton ResourceRegistry provideDefault() {
        return registry;
    }
}
