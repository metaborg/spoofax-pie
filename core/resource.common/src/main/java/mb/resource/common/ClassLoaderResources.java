package mb.resource.common;

import mb.common.result.ThrowingConsumer;
import mb.resource.ReadableResource;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.ClassLoaderToNativeResolver;
import mb.resource.classloader.ClassLoaderUrlResolver;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
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
        return getResource(path).tryAsNativeResource();
    }

    public ReadableResource tryGetAsNativeResource(Class<?> clazz) {
        return getResource(clazz).tryAsNativeResource();
    }

    public ReadableResource tryGetAsNativeResource(SegmentsPath path) {
        return getResource(path).tryAsNativeResource();
    }

    public void performWithResourceLocations(
        String path,
        ThrowingConsumer<FSResource, IOException> directoriesConsumer,
        ThrowingConsumer<JarFileWithPath, IOException> jarFileWithPathConsumer
    ) throws IOException {
        final ClassLoaderResource classLoaderResource = resourceRegistry.getResource(path);
        final ClassLoaderResourceLocations locations = classLoaderResource.getLocations();
        for(FSResource directory : locations.directories) {
            directoriesConsumer.accept(directory);
        }
        for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
            jarFileWithPathConsumer.accept(jarFileWithPath);
        }
    }


    public ClassLoaderResource getDefinitionResource(String path) {
        return definitionDirectory.appendAsRelativePath(path);
    }

    public ReadableResource tryGetAsNativeDefinitionResource(String path) {
        return getDefinitionResource(path).tryAsNativeResource();
    }

    public void performWithDefinitionResourceLocations(
        String path,
        ThrowingConsumer<FSResource, IOException> directoriesConsumer,
        ThrowingConsumer<JarFileWithPath, IOException> jarFileWithPathConsumer
    ) throws IOException {
        final ClassLoaderResource classLoaderResource = definitionDirectory.appendAsRelativePath(path);
        final ClassLoaderResourceLocations locations = classLoaderResource.getLocations();
        for(FSResource directory : locations.directories) {
            directoriesConsumer.accept(directory);
        }
        for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
            jarFileWithPathConsumer.accept(jarFileWithPath);
        }
    }
}
