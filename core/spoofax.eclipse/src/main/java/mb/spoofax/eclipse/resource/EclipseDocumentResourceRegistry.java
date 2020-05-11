package mb.spoofax.eclipse.resource;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.QualifiedResourceKeyString;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import org.checkerframework.checker.nullness.qual.Nullable;

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


    public void putDocumentResource(EclipseDocumentResource resource) {
        logger.trace("Putting document resource '{}'", resource);
        resources.put(resource.getKey().getId(), resource);
    }

    public void removeDocumentResource(EclipseDocumentResource resource) {
        logger.trace("Removing document resource '{}'", resource);
        resources.remove(resource.getKey().getId());
    }


    @Override public String qualifier() {
        return qualifier;
    }


    @Override public Resource getResource(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse document resource with ID '" + id + "'; the ID is not of type String");
        }
        return getResource((String)id);
    }

    @Override public ResourceKey getResourceKey(ResourceKeyString keyStr) {
        if(!keyStr.qualifierMatchesOrMissing(qualifier)) {
            throw new ResourceRuntimeException("Qualifier of '" + keyStr + "' does not match qualifier '" + qualifier + "' of this resource registry");
        }
        return new EclipseDocumentKey(keyStr.getId());
    }

    @Override public Resource getResource(ResourceKeyString keyStr) {
        if(!keyStr.qualifierMatchesOrMissing(qualifier)) {
            throw new ResourceRuntimeException("Qualifier of '" + keyStr + "' does not match qualifier '" + qualifier + "' of this resource registry");
        }
        return getResource(keyStr.getId());
    }

    public Resource getResource(String id) {
        final @Nullable EclipseDocumentResource resource = resources.get(id);
        if(resource != null) {
            return resource;
        } else {
            throw new ResourceRuntimeException("Cannot get Eclipse document resource with ID '" + id + "', it does not exist");
        }
    }

    @Override public QualifiedResourceKeyString toResourceKeyString(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot convert ID '" + id + "' to its string representation; it is not of type String");
        }
        return QualifiedResourceKeyString.of(qualifier, (String)id);
    }

    @Override public String toString(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot convert ID '" + id + "' to its string representation; it is not of type String");
        }
        return QualifiedResourceKeyString.toString(qualifier, (String)id);
    }
}
