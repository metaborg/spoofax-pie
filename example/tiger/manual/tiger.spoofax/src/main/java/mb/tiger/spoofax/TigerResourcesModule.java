package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.tiger.TigerClassloaderResources;

@Module
public class TigerResourcesModule {
    @Provides @TigerResourcesScope
    static TigerClassloaderResources provideClassLoaderResources() {
        return new TigerClassloaderResources();
    }

    @Provides @TigerResourcesScope @TigerQualifier
    static ClassLoaderResourceRegistry provideClassLoaderResourceRegistry(TigerClassloaderResources classLoaderResources) {
        return classLoaderResources.resourceRegistry;
    }

    @Provides @TigerResourcesScope @TigerQualifier("definition-directory")
    static ClassLoaderResource provideDefinitionDirectory(TigerClassloaderResources classLoaderResources) {
        return classLoaderResources.definitionDirectory;
    }

    @Provides @TigerResourcesScope @TigerQualifier("definition-directory")
    static HierarchicalResource provideDefinitionDirectoryAsHierarchicalResource(@TigerQualifier("definition-directory") ClassLoaderResource definitionDirectory) {
        return definitionDirectory;
    }

    @Provides @TigerResourcesScope @TigerQualifier @IntoSet
    static ResourceRegistry provideClassLoaderResourceRegistryIntoSet(@TigerQualifier ClassLoaderResourceRegistry registry) {
        return registry;
    }
}
