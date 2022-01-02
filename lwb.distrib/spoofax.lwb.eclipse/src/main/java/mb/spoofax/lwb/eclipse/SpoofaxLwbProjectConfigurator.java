package mb.spoofax.lwb.eclipse;

import mb.cfg.task.CfgRootDirectoryToObject;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.spoofax.eclipse.util.UncheckedCoreException;
import mb.spoofax.lwb.eclipse.util.JavaProjectUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SpoofaxLwbProjectConfigurator implements ProjectConfigurator {
    @Override public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor) {
        final HashSet<File> rootDirectories = new HashSet<>();
        try {
            new FSResource(root).walkForEach(ResourceMatcher.ofPath(PathMatcher.ofLeaf(CfgRootDirectoryToObject.cfgFileRelativePath)).and(ResourceMatcher.ofFile()), file -> {
                final @Nullable FSResource parent = (FSResource)file.getParent();
                if(parent != null && parent.isDirectory()) {
                    rootDirectories.add(parent.getJavaPath().toFile());
                }
            });
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
        return rootDirectories;
    }

    @Override public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor) {
        return container.getFile(new Path(CfgRootDirectoryToObject.cfgFileRelativePath)).exists();
    }

    @Override public Set<IFolder> getFoldersToIgnore(IProject project, IProgressMonitor monitor) {
        return Collections.emptySet();
    }

    @Override public boolean canConfigure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
        return true;
    }

    @Override public void configure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
        try {
            SpoofaxLwbNature.addTo(project, monitor);
            JavaProjectUtil.configureProject(project, monitor);
        } catch(CoreException e) {
            throw new UncheckedCoreException(e);
        }
    }
}
