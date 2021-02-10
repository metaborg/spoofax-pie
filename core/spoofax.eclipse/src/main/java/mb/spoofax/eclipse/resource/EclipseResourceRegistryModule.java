package mb.spoofax.eclipse.resource;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.spoofax.core.platform.ResourceServiceScope;

import javax.inject.Singleton;

@Module
public abstract class EclipseResourceRegistryModule {
    @Provides @ResourceServiceScope @IntoSet static ResourceRegistry provideEclipseResource(EclipseResourceRegistry registry) {
        return registry;
    }

    @Provides @ResourceServiceScope @IntoSet static ResourceRegistry provideEclipseDocument(EclipseDocumentResourceRegistry registry) {
        return registry;
    }
}
