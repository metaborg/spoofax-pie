package mb.spoofax.lwb.compiler.definition;

import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.spoofax.core.resource.ResourcesComponent;

import java.io.IOException;
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
            final ResourcePath unarchiveDirectory = unarchiveDirectoryBase.appendRelativePath(jarFilePath.getLeaf());
            final Task<?> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, unarchiveMatcher, false, false));
            context.require(task);
            definitionLocations.add(unarchiveDirectory.appendAsRelativePath(jarFileWithPath.path));
        }
        return definitionLocations;
    }
}
