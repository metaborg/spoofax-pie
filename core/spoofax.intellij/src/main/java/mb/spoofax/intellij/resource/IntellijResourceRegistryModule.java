package mb.spoofax.intellij.resource;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;

import javax.inject.Singleton;

@Module
public class IntellijResourceRegistryModule {
    private final IntellijResourceRegistry registry = new IntellijResourceRegistry();

    @Provides @Singleton IntellijResourceRegistry provide() {
        return registry;
    }

    @Provides @Singleton @IntoSet ResourceRegistry provideIntoSet() {
        return registry;
    }
}
