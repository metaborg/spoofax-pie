package mb.spoofax.eclipse.resource;

import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import java.io.Serializable;

public class EclipseResourceRegistry implements ResourceRegistry {
    protected static final String qualifier = "eclipse-resource";
    protected static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    @Override public Serializable qualifier() {
        return qualifier;
    }

    @Override public Resource getResource(Serializable id) {
        if(id instanceof String) {
            final String path = (String) id;
            final @Nullable IResource resource = root.findMember(path);
            if(resource == null) {
                throw new ResourceRuntimeException(
                    "Cannot get Eclipse resource for path '" + path + "'; no resource was found");
            }
            return new EclipseResource(resource);
        } else if(id instanceof IPath) {
            final IPath path = (IPath) id;
            final @Nullable IResource resource = root.findMember(path);
            if(resource == null) {
                throw new ResourceRuntimeException(
                    "Cannot get Eclipse resource for path '" + path + "'; no resource was found");
            }
            return new EclipseResource(resource);
        } else {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse resource with ID '" + id + "'; the ID is not of type String or IPath");
        }
    }

    @Override public Resource getResource(ResourceKey key) {
        final Serializable qualifier = key.qualifier();
        if(!EclipseResourceRegistry.qualifier.equals(qualifier)) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse resource with key '" + key + "'; its qualifier '" + qualifier + "' is not 'eclipse'");
        }
        return getResource(key.id());
    }
}
