package mb.strategolib;

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
import mb.spoofax.core.language.Export;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Stratego library utilities in the context of the Spoofax LWB compiler.
 */
@StrategoLibScope
public class StrategoLibUtil {
    private final StrategoLibClassLoaderResources classLoaderResources;
    private final StrategoLibResourceExports resourceExports;

    @Inject public StrategoLibUtil(
        StrategoLibClassLoaderResources classLoaderResources,
        StrategoLibResourceExports resourceExports
    ) {
        this.classLoaderResources = classLoaderResources;
        this.resourceExports = resourceExports;
    }

    public LinkedHashSet<File> getStrategoLibJavaClassPaths() throws IOException {
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

    public Supplier<StrategoLibInfo> getStrategoLibInfo(
        ResourcePath strategoLibUnarchiveDirectory,
        UnarchiveFromJar unarchiveFromJar
    ) throws IOException {
        for(Export export : resourceExports.strategoExports) {
            final String fileRelativePath = export.caseOf().file(path -> path).otherwiseEmpty()
                .orElseThrow(() -> new IllegalStateException("Stratego 2 standard library export '" + export + "' is not a file export. Only file exports are supported"));
            final ClassLoaderResourceLocations<FSResource> locations = classLoaderResources.definitionDirectory.getLocations();
            for(FSResource directory : locations.directories) {
                final FSResource exportFile = directory.appendAsRelativePath(fileRelativePath);
                if(exportFile.exists()) {
                    return new ValueSupplier<>(new StrategoLibInfo(exportFile.getPath(), new ArrayList<>()));
                }
            }
            for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
                final FSPath jarFilePath = jarFileWithPath.file.getPath();
                @SuppressWarnings("ConstantConditions") // JAR files always have leaves.
                final ResourcePath unarchiveDirectory = strategoLibUnarchiveDirectory.appendRelativePath(jarFilePath.getLeaf());
                final Task<ResourcePath> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, PathStringMatcher.ofExtension("str2lib"), false, false));
                return new StrategoLibInfoSupplier(task.toSupplier(), jarFileWithPath.path, fileRelativePath);
            }
        }
        throw new IOException("Could not get strategolib .str2lib file");
    }

    public static class StrategoLibInfoSupplier implements Supplier<StrategoLibInfo> {
        private final STask<ResourcePath> unarchiveTask;
        private final String path;
        private final String export;

        private StrategoLibInfoSupplier(STask<ResourcePath> unarchiveTask, String path, String export) {
            this.unarchiveTask = unarchiveTask;
            this.path = path;
            this.export = export;
        }

        @Override public StrategoLibInfo get(ExecContext context) {
            final ResourcePath unarchiveDirectory = context.require(unarchiveTask);
            return new StrategoLibInfo(unarchiveDirectory.appendAsRelativePath(path).appendAsRelativePath(export), new ArrayList<>());
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final StrategoLibInfoSupplier that = (StrategoLibInfoSupplier)o;
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
            return "Stratego2LibInfoSupplier{" +
                "unarchiveTask=" + unarchiveTask +
                ", path='" + path + '\'' +
                ", export='" + export + '\'' +
                '}';
        }
    }
}
