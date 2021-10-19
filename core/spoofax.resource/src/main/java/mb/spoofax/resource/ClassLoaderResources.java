package mb.spoofax.resource;

import mb.common.result.ThrowingConsumer;
import mb.resource.ReadableResource;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.ClassLoaderToNativeResolver;
import mb.resource.classloader.ClassLoaderUrlResolver;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.SegmentsPath;

import java.io.IOException;

public class ClassLoaderResources {
    public final ClassLoaderResourceRegistry resourceRegistry;
    public final ClassLoaderResource definitionDirectory;


    public ClassLoaderResources(
        String qualifier,
        ClassLoader classLoader,
        ClassLoaderUrlResolver urlResolver,
        ClassLoaderToNativeResolver toNativeResolver,
        String definitionDirRelativePath
    ) {
        this.resourceRegistry = new ClassLoaderResourceRegistry(qualifier, classLoader, urlResolver, toNativeResolver);
        this.definitionDirectory = resourceRegistry.getResource(definitionDirRelativePath);
    }


    public ClassLoaderResource getResource(String path) {
        return resourceRegistry.getResource(path);
    }

    public ClassLoaderResource getResource(Class<?> clazz) {
        return resourceRegistry.getResource(clazz);
    }

    public ClassLoaderResource getResource(SegmentsPath path) {
        return resourceRegistry.getResource(path);
    }


    public ReadableResource tryGetAsNativeResource(String path) {
        return getResource(path).tryAsNativeFile();
    }

    public ReadableResource tryGetAsNativeResource(Class<?> clazz) {
        return getResource(clazz).tryAsNativeFile();
    }

    public ReadableResource tryGetAsNativeResource(SegmentsPath path) {
        return getResource(path).tryAsNativeFile();
    }


    public void performWithResourceLocations(
        String path,
        ThrowingConsumer<FSResource, IOException> directoriesConsumer,
        ThrowingConsumer<JarFileWithPath<FSResource>, IOException> jarFileWithPathConsumer
    ) throws IOException {
        final ClassLoaderResource classLoaderResource = resourceRegistry.getResource(path);
        final ClassLoaderResourceLocations<FSResource> locations = classLoaderResource.getLocations();
        for(FSResource directory : locations.directories) {
            directoriesConsumer.accept(directory);
        }
        for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
            jarFileWithPathConsumer.accept(jarFileWithPath);
        }
    }

    public void performWithResourceLocationsTryAsNative(
        String path,
        ThrowingConsumer<HierarchicalResource, IOException> directoriesConsumer,
        ThrowingConsumer<JarFileWithPath<HierarchicalResource>, IOException> jarFileWithPathConsumer
    ) throws IOException {
        final ClassLoaderResource classLoaderResource = resourceRegistry.getResource(path);
        final ClassLoaderResourceLocations<HierarchicalResource> locations = classLoaderResource.getLocationsTryAsNative();
        for(HierarchicalResource directory : locations.directories) {
            directoriesConsumer.accept(directory);
        }
        for(JarFileWithPath<HierarchicalResource> jarFileWithPath : locations.jarFiles) {
            jarFileWithPathConsumer.accept(jarFileWithPath);
        }
    }


    public ClassLoaderResource getDefinitionResource(String path) {
        return definitionDirectory.appendAsRelativePath(path);
    }

    public ReadableResource tryGetAsNativeDefinitionResource(String path) {
        return getDefinitionResource(path).tryAsNativeFile();
    }

    public void performWithDefinitionResourceLocations(
        String path,
        ThrowingConsumer<FSResource, IOException> directoriesConsumer,
        ThrowingConsumer<JarFileWithPath<FSResource>, IOException> jarFileWithPathConsumer
    ) throws IOException {
        final ClassLoaderResource classLoaderResource = definitionDirectory.appendAsRelativePath(path);
        final ClassLoaderResourceLocations<FSResource> locations = classLoaderResource.getLocations();
        for(FSResource directory : locations.directories) {
            directoriesConsumer.accept(directory);
        }
        for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
            jarFileWithPathConsumer.accept(jarFileWithPath);
        }
    }

    public void performWithDefinitionResourceLocationsTryAsNative(
        String path,
        ThrowingConsumer<HierarchicalResource, IOException> directoriesConsumer,
        ThrowingConsumer<JarFileWithPath<HierarchicalResource>, IOException> jarFileWithPathConsumer
    ) throws IOException {
        final ClassLoaderResource classLoaderResource = definitionDirectory.appendAsRelativePath(path);
        final ClassLoaderResourceLocations<HierarchicalResource> locations = classLoaderResource.getLocationsTryAsNative();
        for(HierarchicalResource directory : locations.directories) {
            directoriesConsumer.accept(directory);
        }
        for(JarFileWithPath<HierarchicalResource> jarFileWithPath : locations.jarFiles) {
            jarFileWithPathConsumer.accept(jarFileWithPath);
        }
    }
}
