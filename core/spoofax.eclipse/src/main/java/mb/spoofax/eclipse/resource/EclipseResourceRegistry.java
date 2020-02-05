package mb.spoofax.eclipse.resource;

import mb.resource.Resource;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.Serializable;

@Singleton
public class EclipseResourceRegistry implements ResourceRegistry {
    static final String qualifier = "eclipse-resource";


    @Inject public EclipseResourceRegistry() { }


    @Override public String qualifier() {
        return qualifier;
    }


    @Override public EclipseResource getResource(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse resource with ID '" + id + "'; the ID is not of type String");
        }
        return getResource((String)id);
    }


    @Override public EclipseResourcePath getResourceKey(String idStr) {
        return new EclipseResourcePath(idStr);
    }

    @Override public EclipseResource getResource(String idStr) {
        return new EclipseResource(new EclipseResourcePath(idStr));
    }

    @Override public String toStringRepresentation(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot convert ID '" + id + "' to its string representation; it is not of type String");
        }
        return (String)id;
    }

    @Override public @Nullable File toLocalFile(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot attempt to convert identifier '" + id + "' to a local file; the ID is not of type String");
        }
        final EclipseResourcePath path = getResourceKey((String)id);
        return path.path.toFile();
    }

    @Override public @Nullable File toLocalFile(Resource resource) {
        if(!(resource instanceof EclipseResource)) {
            throw new ResourceRuntimeException(
                "Cannot attempt to convert resource '" + resource + "' to a local file; the resource is not of type EclipseResource");
        }
        final EclipseResource eclipseResource = (EclipseResource)resource;
        return eclipseResource.path.path.toFile();
    }
}
