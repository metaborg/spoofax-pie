package mb.spoofax.eclipse.resource;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;

import javax.inject.Singleton;

@Module
public class EclipseResourceRegistryModule {
    @Provides @Singleton @IntoSet ResourceRegistry provideIntoSet(EclipseResourceRegistry registry) {
        return registry;
    }
}
