package mb.spoofax.eclipse.resource;

import mb.resource.ResourceRuntimeException;
import mb.resource.classloader.ClassLoaderToNativeResolver;
import mb.resource.dagger.ResourceServiceScope;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@ResourceServiceScope
public class EclipseClassLoaderToNativeResolver implements ClassLoaderToNativeResolver {
    private final EclipseResourceRegistry resourceRegistry;
    private final IWorkspaceRoot workspaceRoot;

    @Inject public EclipseClassLoaderToNativeResolver(EclipseResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
        this.workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    }

    @Override public @Nullable HierarchicalResource toNativeFile(URL url) {
        final URI uri = toUri(url);
        if(!isSchemeCompatible(uri)) {
            return null;
        }
        final IFile[] files = workspaceRoot.findFilesForLocationURI(uri);
        if(files.length == 1) { // Found one file in the Eclipse workspace.
            return resourceRegistry.getResource(files[0]);
        }
        return null; // Not found in the Eclipse workspace.
    }

    @Override public @Nullable HierarchicalResource toNativeDirectory(URL url) {
        final URI uri = toUri(url);
        if(!isSchemeCompatible(uri)) {
            return null;
        }
        final IContainer[] containers = workspaceRoot.findContainersForLocationURI(uri);
        if(containers.length == 1) { // Found one container in the Eclipse workspace.
            return resourceRegistry.getResource(containers[0]);
        }
        return null; // Not found in the Eclipse workspace.
    }

    private boolean isSchemeCompatible(URI uri) {
        final @Nullable String scheme = uri.getScheme();
        if(scheme == null) { // No scheme: cannot determine native resource.
            return false;
        }
        try {
            EFS.getFileSystem(scheme);
        } catch(CoreException e) { // No file system for scheme: cannot determine native resource.
            return false;
        }
        return true;
    }

    private URI toUri(URL url) {
        try {
            return url.toURI();
        } catch(URISyntaxException e) {
            throw new ResourceRuntimeException("Cannot convert URL to native resource; could not convert URL '" + url + "' to an URI", e);
        }
    }
}
