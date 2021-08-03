package mb.spoofax.eclipse.util;

import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.resource.WrapsEclipseResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

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


    public static Optional<File> asLocalFile(IResource resource) {
        @Nullable IPath path = resource.getRawLocation();
        if(path == null) {
            path = resource.getLocation();
        }
        if(path == null) {
            return Optional.empty();
        }
        return Optional.of(toLocalFile(path));
    }

    public static File toLocalFile(IResource resource) {
        return asLocalFile(resource)
            .orElseThrow(() -> new ResourceRuntimeException("Cannot convert Eclipse resource '" + resource + "' to a local file"));
    }

    public static File toLocalFile(IPath path) {
        return path.makeAbsolute().toFile();
    }

    public static Optional<FSResource> asFsResource(IResource resource) {
        return asLocalFile(resource).map(FSResource::new);
    }

    public static FSResource toFsResource(IResource resource) {
        return asFsResource(resource)
            .orElseThrow(() -> new ResourceRuntimeException("Cannot convert Eclipse resource '" + resource + "' to a Java filesystem resource"));
    }

    public static FSResource toFsResource(IPath path) {
        return new FSResource(toLocalFile(path));
    }

    public static Optional<FSPath> asFsPath(IResource resource) {
        return asLocalFile(resource).map(FSPath::new);
    }

    public static FSPath toFsPath(IResource resource) {
        return asFsPath(resource)
            .orElseThrow(() -> new ResourceRuntimeException("Cannot convert Eclipse resource '" + resource + "' to a Java filesystem path"));
    }

    public static FSPath toFsPath(IPath path) {
        return new FSPath(toLocalFile(path));
    }
}
