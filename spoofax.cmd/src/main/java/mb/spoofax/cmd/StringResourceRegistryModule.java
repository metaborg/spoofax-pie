package mb.spoofax.cmd;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.string.StringResourceRegistry;

import javax.inject.Singleton;

@Module
public class StringResourceRegistryModule {
    private final StringResourceRegistry registry = new StringResourceRegistry();

    @Provides @Singleton StringResourceRegistry provide() {
        return registry;
    }

    @Provides @Singleton @IntoSet ResourceRegistry provideIntoSet() {
        return registry;
    }
}
