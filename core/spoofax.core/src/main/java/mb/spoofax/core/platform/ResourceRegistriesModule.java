package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.text.TextResourceRegistry;
import mb.resource.url.URLResourceRegistry;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Module
public class ResourceRegistriesModule {
    private final Set<ResourceRegistry> additionalRegistries;

    public ResourceRegistriesModule(Set<ResourceRegistry> additionalRegistries) {
        this.additionalRegistries = additionalRegistries;
    }

    public ResourceRegistriesModule(ResourceRegistry... additionalRegistries) {
        this(new HashSet<>(Arrays.asList(additionalRegistries)));
    }

    public ResourceRegistriesModule() {
        this(new HashSet<>());
    }


    @Provides @Singleton
    static FSResourceRegistry provideFsResourceRegistry() {
        return new FSResourceRegistry();
    }

    @Provides @Singleton @IntoSet
    static ResourceRegistry provideFsResourceRegistryIntoSet(FSResourceRegistry registry) {
        return registry;
    }

    @Provides @Named("default") @Singleton
    static ResourceRegistry provideFsResourceRegistryAsDefault(FSResourceRegistry registry) {
        return registry;
    }


    @Provides @Singleton
    static ClassLoaderResourceRegistry provideClassLoaderResourceRegistry() {
        return new ClassLoaderResourceRegistry();
    }

    @Provides @Singleton @IntoSet
    static ResourceRegistry provideClassLoaderResourceRegistryIntoSet(ClassLoaderResourceRegistry registry) {
        return registry;
    }


    @Provides @Singleton
    static URLResourceRegistry provideUrlResourceRegistry() {
        return new URLResourceRegistry();
    }

    @Provides @Singleton @IntoSet
    static ResourceRegistry provideUrlResourceRegistryIntoSet(URLResourceRegistry registry) {
        return registry;
    }


    @Provides @Singleton @Platform
    static TextResourceRegistry provideQualifiedTextResourceRegistry() {
        return new TextResourceRegistry();
    }

    @Provides @Singleton
    static TextResourceRegistry provideTextResourceRegistry(@Platform TextResourceRegistry registry) {
        return registry;
    }

    @Provides @Singleton @IntoSet
    static ResourceRegistry provideTextResourceRegistryIntoSet(@Platform TextResourceRegistry registry) {
        return registry;
    }


    @Provides @Singleton @ElementsIntoSet
    Set<ResourceRegistry> provideAdditionalResourceRegistries() {
        return additionalRegistries;
    }
}
