package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.text.TextResourceRegistry;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class ResourceRegistriesModule {
    private final FSResourceRegistry fsResourceRegistry = new FSResourceRegistry();
    private final TextResourceRegistry textResourceRegistry = new TextResourceRegistry();


    @Provides @Singleton FSResourceRegistry provideFsResourceRegistry() {
        return fsResourceRegistry;
    }

    @Provides @Singleton @IntoSet ResourceRegistry provideFsResourceRegistryIntoSet() {
        return fsResourceRegistry;
    }

    @Provides @Named("default") @Singleton ResourceRegistry provideFsResourceRegistryAsDefault() {
        return fsResourceRegistry;
    }


    @Provides @Singleton TextResourceRegistry provideTextResourceRegistry() {
        return textResourceRegistry;
    }

    @Provides @Singleton @IntoSet ResourceRegistry provideTextResourceRegistryIntoSet() {
        return textResourceRegistry;
    }
}
