package mb.spoofax.intellij.resource;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.spoofax.core.platform.ResourceServiceScope;

@Module
public abstract class IntellijResourceRegistryModule {
    @Provides @ResourceServiceScope static IntellijResourceRegistry provide() {
        return new IntellijResourceRegistry();
    }

    @Provides @ResourceServiceScope @IntoSet static ResourceRegistry provideIntoSet(IntellijResourceRegistry registry) {
        return registry;
    }
}
