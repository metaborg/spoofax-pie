package mb.spoofax.eclipse.util;

import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.resource.WrapsEclipseResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import javax.inject.Inject;

@PlatformScope
public class ResourceUtil {
    private final ResourceService resourceService;


    @Inject public ResourceUtil(ResourceService resourceService) {
        this.resourceService = resourceService;
    }


    public IResource getEclipseResource(ResourceKey resourceKey) {
        return getEclipseResource(resourceService.getResource(resourceKey));
    }

    public IResource getEclipseResource(Resource resource) {
        if(!(resource instanceof WrapsEclipseResource)) {
            throw new ResourceRuntimeException("Resource '" + resource + "' does not wrap an Eclipse resource");
        }
        final @Nullable IResource eclipseResource = ((WrapsEclipseResource)resource).getWrappedEclipseResource();
        if(eclipseResource == null) {
            throw new ResourceRuntimeException("Resource '" + resource + "' wraps an Eclipse resource, but the wrapped resource is null");
        }
        return eclipseResource;
    }

    public IFile getEclipseFile(ResourceKey resourceKey) {
        return getEclipseFile(resourceService.getResource(resourceKey));
    }

    public IFile getEclipseFile(Resource resource) {
        final IResource eclipseResource = getEclipseResource(resource);
        if(!(eclipseResource instanceof IFile)) {
            throw new ResourceRuntimeException("Resource '" + resource + "' wraps an Eclipse resource, but the wrapped resource is not a file");
        }
        return (IFile)eclipseResource;
    }
}
