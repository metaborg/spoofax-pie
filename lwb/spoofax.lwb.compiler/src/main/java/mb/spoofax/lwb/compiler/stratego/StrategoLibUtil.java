package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.CompileLanguageSpecificationShared;
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
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.strategolib.StrategoLibClassLoaderResources;
import mb.strategolib.StrategoLibExports;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class StrategoLibUtil {
    private final StrategoLibClassLoaderResources strategoLibClassLoaderResources;
    private final UnarchiveFromJar unarchiveFromJar;

    @Inject public StrategoLibUtil(
        StrategoLibClassLoaderResources strategoLibClassLoaderResources,
        UnarchiveFromJar unarchiveFromJar
    ) {
        this.strategoLibClassLoaderResources = strategoLibClassLoaderResources;
        this.unarchiveFromJar = unarchiveFromJar;
    }

    public LinkedHashSet<File> getStrategoLibJavaClassPaths() throws IOException {
        final LinkedHashSet<File> javaClassPaths = new LinkedHashSet<>();
        final ClassLoaderResourceLocations<FSResource> locations = strategoLibClassLoaderResources.definitionDirectory.getLocations();
        for(FSResource directory : locations.directories) {
            javaClassPaths.add(directory.getJavaPath().toFile());
        }
        for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
            javaClassPaths.add(jarFileWithPath.file.getPath().getJavaPath().toFile());
        }
        return javaClassPaths;
    }

    public Supplier<Stratego2LibInfo> getStrategoLibInfo(
        CompileLanguageSpecificationShared compileLanguageSpecificationShared
    ) throws IOException {
        for(String export : StrategoLibExports.getStr2LibExports()) {
            final ClassLoaderResourceLocations<FSResource> locations = strategoLibClassLoaderResources.definitionDirectory.getLocations();
            for(FSResource directory : locations.directories) {
                final FSResource exportFile = directory.appendAsRelativePath(export);
                if(exportFile.exists()) {
                    return new ValueSupplier<>(new Stratego2LibInfo(exportFile.getPath(), new ArrayList<>()));
                }
            }
            final ResourcePath unarchiveDirectoryBase = compileLanguageSpecificationShared.unarchiveDirectory().appendRelativePath("strategoLib");
            for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
                final FSPath jarFilePath = jarFileWithPath.file.getPath();
                @SuppressWarnings("ConstantConditions") // JAR files always have leaves.
                final ResourcePath unarchiveDirectory = unarchiveDirectoryBase.appendRelativePath(jarFilePath.getLeaf());
                final Task<ResourcePath> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, PathStringMatcher.ofExtension("str2lib"), false, false));
                return new Stratego2LibInfoSupplier(task.toSupplier(), jarFileWithPath.path, export);
            }
        }
        throw new IOException("Could not get strategolib .str2lib file");
    }

    public static class Stratego2LibInfoSupplier implements Supplier<Stratego2LibInfo> {
        private final STask<ResourcePath> unarchiveTask;
        private final String path;
        private final String export;

        private Stratego2LibInfoSupplier(STask<ResourcePath> unarchiveTask, String path, String export) {
            this.unarchiveTask = unarchiveTask;
            this.path = path;
            this.export = export;
        }

        @Override public Stratego2LibInfo get(ExecContext context) {
            final ResourcePath unarchiveDirectory = context.require(unarchiveTask);
            return new Stratego2LibInfo(unarchiveDirectory.appendAsRelativePath(path).appendAsRelativePath(export), new ArrayList<>());
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Stratego2LibInfoSupplier that = (Stratego2LibInfoSupplier)o;
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
