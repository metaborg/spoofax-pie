package mb.spoofax.eclipse.resource;

import mb.resource.ReadableResource;
import mb.resource.ResourceRuntimeException;
import mb.resource.classloader.ClassLoaderToNativeResolver;
import mb.resource.dagger.ResourceServiceScope;
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

    @Override public @Nullable ReadableResource toNativeResource(URL url) {
        try {
            final URI uri = url.toURI();
            final IFile[] files = workspaceRoot.findFilesForLocationURI(uri);
            if(files.length == 1) {
                return resourceRegistry.getResource(files[0]);
            }
            final IContainer[] containers = workspaceRoot.findContainersForLocationURI(uri);
            if(containers.length == 1) {
                return resourceRegistry.getResource(containers[0]);
            }
            return null;
        } catch(URISyntaxException e) {
            throw new ResourceRuntimeException("Could not convert URL '" + url + "' to an URI", e);
        }
    }
}
