package mb.gpp;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.ValueSupplier;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * GPP Stratego library utilities in the context of the Spoofax LWB compiler.
 */
@GppScope
public class GppUtil {
    private final GppClassLoaderResources classLoaderResources;

    @Inject public GppUtil(
        GppClassLoaderResources classLoaderResources
    ) {
        this.classLoaderResources = classLoaderResources;
    }

    public LinkedHashSet<File> getGppJavaClassPaths() throws IOException {
        final LinkedHashSet<File> javaClassPaths = new LinkedHashSet<>();
        final ClassLoaderResourceLocations<FSResource> locations = classLoaderResources.definitionDirectory.getLocations();
        for(FSResource directory : locations.directories) {
            javaClassPaths.add(directory.getJavaPath().toFile());
        }
        for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
            javaClassPaths.add(jarFileWithPath.file.getPath().getJavaPath().toFile());
        }
        return javaClassPaths;
    }

    public Supplier<GppInfo> getGppInfo(
        ResourcePath gppUnarchiveDirectory,
        UnarchiveFromJar unarchiveFromJar
    ) throws IOException {
        for(String export : GppExports.getStr2LibExports()) {
            final ClassLoaderResourceLocations<FSResource> locations = classLoaderResources.definitionDirectory.getLocations();
            for(FSResource directory : locations.directories) {
                final FSResource exportFile = directory.appendAsRelativePath(export);
                if(exportFile.exists()) {
                    return new ValueSupplier<>(new GppInfo(exportFile.getPath(), new ArrayList<>()));
                }
            }
            for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
                final FSPath jarFilePath = jarFileWithPath.file.getPath();
                @SuppressWarnings("ConstantConditions") // JAR files always have leaves.
                final ResourcePath unarchiveDirectory = gppUnarchiveDirectory.appendRelativePath(jarFilePath.getLeaf());
                final Task<ResourcePath> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, PathStringMatcher.ofExtension("str2lib"), false, false));
                return new GppInfoSupplier(task.toSupplier(), jarFileWithPath.path, export);
            }
        }
        throw new IOException("Could not get Gpp .str2lib file");
    }

    public static class GppInfoSupplier implements Supplier<GppInfo> {
        private final STask<ResourcePath> unarchiveTask;
        private final String path;
        private final String export;

        private GppInfoSupplier(STask<ResourcePath> unarchiveTask, String path, String export) {
            this.unarchiveTask = unarchiveTask;
            this.path = path;
            this.export = export;
        }

        @Override public GppInfo get(ExecContext context) {
            final ResourcePath unarchiveDirectory = context.require(unarchiveTask);
            return new GppInfo(unarchiveDirectory.appendAsRelativePath(path).appendAsRelativePath(export), new ArrayList<>());
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final GppInfoSupplier that = (GppInfoSupplier)o;
            if(!unarchiveTask.equals(that.unarchiveTask)) return false;
            if(!path.equals(that.path)) return false;
            return export.equals(that.export);
        }

        @Override public int hashCode() {
            int result = unarchiveTask.hashCode();
            result = 31 * result + path.hashCode();
            result = 31 * result + export.hashCode();
            return result;
        }

        @Override public String toString() {
            return "GppInfoSupplier{" +
                "unarchiveTask=" + unarchiveTask +
                ", path='" + path + '\'' +
                ", export='" + export + '\'' +
                '}';
        }
    }
}
