package mb.spoofax.lwb.compiler.definition;

import mb.common.function.Action1;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.spoofax.core.resource.ResourcesComponent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;

public class UnarchiveUtil {
    public static LinkedHashSet<ResourcePath> unarchive(
        ExecContext context,
        ResourcesComponent resourcesComponent,
        ResourcePath unarchiveDirectoryBase,
        UnarchiveFromJar unarchiveFromJar,
        PathStringMatcher unarchiveMatcher
    ) throws IOException {
        final LinkedHashSet<ResourcePath> definitionLocations = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        final ClassLoaderResourceLocations<FSResource> locations = resourcesComponent.getDefinitionDirectory().getLocations();
        for(FSResource directory : locations.directories) {
            definitionLocations.add(directory.getPath());
        }
        for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
            final ResourcePath jarFilePath = jarFileWithPath.file.getPath();
            @SuppressWarnings("ConstantConditions") // JAR files always have leaves.
            final ResourcePath unarchiveDirectory = unarchiveDirectoryBase.appendRelativePath(jarFilePath.getLeaf()).getNormalized();
            final Task<?> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, unarchiveMatcher, false, false));
            context.require(task);
            definitionLocations.add(unarchiveDirectory.appendAsRelativePath(jarFileWithPath.path));
        }
        return definitionLocations;
    }

    public static void resolveDirectory(
        String relativePath,
        LinkedHashSet<ResourcePath> unarchivedDefinitionLocations,
        ResourceService resourceService,
        String metaLanguageDisplayName,
        Action1<HierarchicalResource> resolve
    ) {
        boolean found = false;
        for(ResourcePath definitionDirectory : unarchivedDefinitionLocations) {
            final ResourcePath directoryPath = definitionDirectory.appendAsRelativePath(relativePath);
            final HierarchicalResource directory = resourceService.getHierarchicalResource(directoryPath);
            try {
                if(directory.exists() && directory.isDirectory()) {
                    resolve.apply(directory);
                    found = true;
                }
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        if(!found) {
            throw new UncheckedIOException(new IOException(metaLanguageDisplayName + "directory export '" + relativePath + "' was not found in any of its definition locations: " + unarchivedDefinitionLocations));
        }
    }
}
