package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.ClassLoaderToNativeResolver;
import mb.resource.classloader.ClassLoaderUrlResolver;
import mb.resource.hierarchical.HierarchicalResource;
import mb.tiger.TigerClassloaderResources;

@Module
public class TigerResourcesModule {
    private final ClassLoaderUrlResolver urlResolver;
    private final ClassLoaderToNativeResolver toNativeResolver;


    public TigerResourcesModule(ClassLoaderUrlResolver urlResolver, ClassLoaderToNativeResolver toNativeResolver) {
        this.urlResolver = urlResolver;
        this.toNativeResolver = toNativeResolver;
    }

    public TigerResourcesModule() {
        this(ClassLoaderResourceRegistry.defaultUrlResolver, ClassLoaderResourceRegistry.defaultToNativeResolver);
    }


    @Provides @TigerResourcesScope
    TigerClassloaderResources provideClassLoaderResources() {
        return new TigerClassloaderResources(urlResolver, toNativeResolver);
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
