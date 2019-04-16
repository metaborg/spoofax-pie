package mb.spoofax.cmd;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.string.StringResourceRegistry;

import javax.inject.Singleton;

@Module
public class StringRegistryModule {
    private final StringResourceRegistry registry = new StringResourceRegistry();

    @Provides @Singleton StringResourceRegistry provideStringResourceRegistry() {
        return registry;
    }

    @Provides @Singleton @IntoSet ResourceRegistry provideStringResourceRegistryIntoSet() {
        return registry;
    }
}
