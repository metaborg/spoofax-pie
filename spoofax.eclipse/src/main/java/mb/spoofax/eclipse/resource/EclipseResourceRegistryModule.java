package mb.spoofax.eclipse.resource;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;

import javax.inject.Singleton;

@Module
public class EclipseResourceRegistryModule {
    @Provides @Singleton @IntoSet ResourceRegistry provideEclipseResource(EclipseResourceRegistry registry) {
        return registry;
    }

    @Provides @Singleton @IntoSet ResourceRegistry provideEclipseDocument(EclipseDocumentResourceRegistry registry) {
        return registry;
    }
}
