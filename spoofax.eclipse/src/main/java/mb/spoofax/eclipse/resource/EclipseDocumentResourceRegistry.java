package mb.spoofax.eclipse.resource;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.Resource;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class EclipseDocumentResourceRegistry implements ResourceRegistry {
    static final String qualifier = "eclipse-document";

    private final Logger logger;
    private final ConcurrentHashMap<String, EclipseDocumentResource> resources = new ConcurrentHashMap<>();


    @Inject public EclipseDocumentResourceRegistry(LoggerFactory loggerFactory) {
        this.logger = loggerFactory.create(getClass());
    }


    public <D extends IDocument & IDocumentExtension4> void putDocumentResource(EclipseDocumentResource resource) {
        logger.trace("Putting document resource '{}'", resource);
        resources.put(resource.getKey().getId(), resource);
    }

    public void removeDocumentResource(EclipseDocumentResource resource) {
        logger.trace("Removing document resource '{}'", resource);
        resources.remove(resource.getKey().getId());
    }


    @Override public Resource getResource(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse document resource with ID '" + id + "'; the ID is not of type String");
        }
        return getResource((String) id);
    }

    @Override public Resource getResource(String id) {
        final @Nullable EclipseDocumentResource resource = resources.get(id);
        if(resource != null) {
            return resource;
        } else {
            throw new ResourceRuntimeException("Cannot get Eclipse document resource with ID '" + id + "', it does not exist");
        }
    }

    @Override public String qualifier() {
        return qualifier;
    }

    @Override public String toStringRepresentation(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot convert ID '" + id + "' to its string representation; it is not of type String");
        }
        return (String) id;
    }
}
