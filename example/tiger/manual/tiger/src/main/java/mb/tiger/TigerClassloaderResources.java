package mb.tiger;

import mb.common.result.ThrowingConsumer;
import mb.resource.ReadableResource;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.SegmentsPath;

import java.io.IOException;

public class TigerClassloaderResources {
    public final ClassLoaderResourceRegistry resourceRegistry = new ClassLoaderResourceRegistry("mb-tiger-classloader-resource", TigerClassloaderResources.class.getClassLoader());

    public ClassLoaderResource getResource(String path) {
        return resourceRegistry.getResource(path);
    }

    public ClassLoaderResource getResource(Class<?> clazz) {
        return resourceRegistry.getResource(clazz);
    }

    public ClassLoaderResource getResource(SegmentsPath path) {
        return resourceRegistry.getResource(path);
    }

    public ReadableResource tryGetAsLocalResource(String path) {
        return getResource(path).tryAsLocalResource();
    }

    public ReadableResource tryGetAsLocalResource(Class<?> clazz) {
        return getResource(clazz).tryAsLocalResource();
    }

    public ReadableResource tryGetAsLocalResource(SegmentsPath path) {
        return getResource(path).tryAsLocalResource();
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


    public final ClassLoaderResource definitionDirectory = resourceRegistry.getResource("mb/tiger");

    public ClassLoaderResource getDefinitionResource(String path) {
        return definitionDirectory.appendAsRelativePath(path);
    }

    public ReadableResource tryGetAsLocalDefinitionResource(String path) {
        return getDefinitionResource(path).tryAsLocalResource();
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
