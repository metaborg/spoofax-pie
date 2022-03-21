package mb.tiger.spoofax;

import dagger.Component;
import mb.resource.ResourceRegistry;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.core.resource.ResourcesComponent;
import mb.tiger.TigerClassloaderResources;

import java.util.Set;

@TigerResourcesScope @Component(modules = TigerResourcesModule.class)
public interface TigerResourcesComponent extends ResourcesComponent, ResourceRegistriesProvider {
    @Override TigerClassloaderResources getClassloaderResources();

    @Override @TigerQualifier ClassLoaderResourceRegistry getClassLoaderResourceRegistry();

    @Override @TigerQualifier("definition-directory") ClassLoaderResource getDefinitionDirectory();

    @Override @TigerQualifier("definition-directory") HierarchicalResource getDefinitionDirectoryAsHierarchicalResource();

    @Override @TigerQualifier Set<ResourceRegistry> getResourceRegistries();
}
