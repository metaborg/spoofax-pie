package mb.spoofax.core.resource;

import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.resource.ClassLoaderResources;

public interface ResourcesComponent extends ResourceRegistriesProvider, AutoCloseable {
    ClassLoaderResources getClassloaderResources();

    ClassLoaderResourceRegistry getClassLoaderResourceRegistry();

    ClassLoaderResource getDefinitionDirectory();

    HierarchicalResource getDefinitionDirectoryAsHierarchicalResource();


    @Override default void close() {
        // Override to make Dagger not treat this as a component method.
    }
}
