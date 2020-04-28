package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.text.TextResourceRegistry;
import mb.resource.url.URLResourceRegistry;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class ResourceRegistriesModule {
    private final FSResourceRegistry fsResourceRegistry = new FSResourceRegistry();
    private final ClassLoaderResourceRegistry classLoaderResourceRegistry = new ClassLoaderResourceRegistry();
    private final URLResourceRegistry urlResourceRegistry = new URLResourceRegistry();
    private final TextResourceRegistry textResourceRegistry = new TextResourceRegistry();


    @Provides @Singleton
    FSResourceRegistry provideFsResourceRegistry() {
        return fsResourceRegistry;
    }

    @Provides @Singleton @IntoSet
    ResourceRegistry provideFsResourceRegistryIntoSet() {
        return fsResourceRegistry;
    }

    @Provides @Named("default") @Singleton
    ResourceRegistry provideFsResourceRegistryAsDefault() {
        return fsResourceRegistry;
    }


    @Provides @Singleton
    ClassLoaderResourceRegistry provideClassLoaderResourceRegistry() {
        return classLoaderResourceRegistry;
    }

    @Provides @Singleton @IntoSet
    ResourceRegistry provideClassLoaderResourceRegistryIntoSet() {
        return classLoaderResourceRegistry;
    }


    @Provides @Singleton
    URLResourceRegistry provideUrlResourceRegistry() {
        return urlResourceRegistry;
    }

    @Provides @Singleton @IntoSet
    ResourceRegistry provideUrlResourceRegistryIntoSet() {
        return urlResourceRegistry;
    }


    @Provides @Singleton
    TextResourceRegistry provideTextResourceRegistry() {
        return textResourceRegistry;
    }

    @Provides @Singleton @IntoSet
    ResourceRegistry provideTextResourceRegistryIntoSet() {
        return textResourceRegistry;
    }
}
