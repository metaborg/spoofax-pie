package mb.tiger.spoofax;

import dagger.Component;
import mb.resource.ResourceRegistry;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.core.language.LanguageResourcesComponent;
import mb.tiger.TigerClassloaderResources;

import java.util.Set;

@TigerResourcesScope @Component(modules = TigerResourcesModule.class)
public interface TigerResourcesComponent extends LanguageResourcesComponent {
    TigerClassloaderResources getClassloaderResources();

    @TigerQualifier ClassLoaderResourceRegistry getClassLoaderResourceRegistry();

    @TigerQualifier("definition-directory") ClassLoaderResource getDefinitionDirectory();

    @TigerQualifier("definition-directory") HierarchicalResource getDefinitionDirectoryAsHierarchicalResource();

    @TigerQualifier Set<ResourceRegistry> getResourceRegistries();
}
