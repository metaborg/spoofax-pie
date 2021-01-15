package mb.spoofax.dynamicloading;

import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;

public class DynamicResourceService implements ResourceService {
    private ResourceService resourceService;

    public DynamicResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Override
    public Resource getResource(ResourceKey key) {
        return resourceService.getResource(key);
    }

    @Override
    public ReadableResource getReadableResource(ResourceKey key) {
        return resourceService.getReadableResource(key);
    }

    @Override
    public WritableResource getWritableResource(ResourceKey key) {
        return resourceService.getWritableResource(key);
    }

    @Override
    public HierarchicalResource getHierarchicalResource(ResourcePath path) {
        return resourceService.getHierarchicalResource(path);
    }

    @Override
    public ResourceKey getResourceKey(ResourceKeyString keyStr) {
        return resourceService.getResourceKey(keyStr);
    }

    @Override
    public ResourcePath getResourcePath(ResourceKeyString pathStr) {
        return resourceService.getResourcePath(pathStr);
    }

    @Override
    public Resource getResource(ResourceKeyString keyStr) {
        return resourceService.getResource(keyStr);
    }

    @Override
    public ReadableResource getReadableResource(ResourceKeyString keyStr) {
        return resourceService.getReadableResource(keyStr);
    }

    @Override
    public WritableResource getWritableResource(ResourceKeyString keyStr) {
        return resourceService.getWritableResource(keyStr);
    }

    @Override
    public HierarchicalResource getHierarchicalResource(ResourceKeyString pathStr) {
        return resourceService.getHierarchicalResource(pathStr);
    }

    @Override
    public Resource appendOrReplaceWith(HierarchicalResource resource, String keyStrOrPath) {
        return resourceService.appendOrReplaceWith(resource, keyStrOrPath);
    }

    @Override
    public HierarchicalResource appendOrReplaceWithHierarchical(HierarchicalResource resource, String pathStrOrPath) {
        return resourceService.appendOrReplaceWithHierarchical(resource, pathStrOrPath);
    }

    @Override
    public @Nullable File toLocalFile(ResourceKey key) {
        return resourceService.toLocalFile(key);
    }

    @Override
    public @Nullable File toLocalFile(Resource resource) {
        return resourceService.toLocalFile(resource);
    }

    @Override
    public ResourceRegistry getDefaultResourceRegistry() {
        return resourceService.getDefaultResourceRegistry();
    }

    @Override
    public @Nullable ResourceRegistry getResourceRegistry(String qualifier) {
        return resourceService.getResourceRegistry(qualifier);
    }

    @Override
    public ResourceService createChild(ResourceRegistry defaultRegistry, Iterable<ResourceRegistry> registries,
                                       Iterable<ResourceService> additionalAncestors) {
        return resourceService.createChild(defaultRegistry, registries, additionalAncestors);
    }
}
