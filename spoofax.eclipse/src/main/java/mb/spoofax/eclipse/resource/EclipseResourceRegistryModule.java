package mb.spoofax.eclipse.resource;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;

import javax.inject.Singleton;

@Module
public class EclipseResourceRegistryModule {
    private final EclipseResourceRegistry registry = new EclipseResourceRegistry();

    @Provides @Singleton EclipseResourceRegistry provide() {
        return registry;
    }

    @Provides @Singleton @IntoSet ResourceRegistry provideIntoSet() {
        return registry;
    }
}
