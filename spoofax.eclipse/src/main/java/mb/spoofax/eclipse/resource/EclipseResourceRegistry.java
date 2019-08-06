package mb.spoofax.eclipse.resource;

import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class EclipseResourceRegistry implements ResourceRegistry {
    static final String qualifier = "eclipse-resource";


    @Inject public EclipseResourceRegistry() { }


    @Override public String qualifier() {
        return qualifier;
    }


    @Override public Resource getResource(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse resource with ID '" + id + "'; the ID is not of type String");
        }
        return getResource((String) id);
    }


    @Override public ResourceKey getResourceKey(String idStr) {
        return new EclipseResourcePath(idStr);
    }

    @Override public Resource getResource(String idStr) {
        return new EclipseResource(new EclipseResourcePath(idStr));
    }


    @Override public String toStringRepresentation(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot convert ID '" + id + "' to its string representation; it is not of type String");
        }
        return (String) id;
    }
}
