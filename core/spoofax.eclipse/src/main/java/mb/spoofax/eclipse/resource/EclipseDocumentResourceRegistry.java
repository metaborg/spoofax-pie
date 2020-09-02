package mb.spoofax.eclipse.resource;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
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


    @Override public ResourceKey getResourceKey(ResourceKeyString keyStr) {
        if(!keyStr.qualifierMatchesOrMissing(qualifier)) {
            throw new ResourceRuntimeException("Qualifier of '" + keyStr + "' does not match qualifier '" + qualifier + "' of this resource registry");
        }
        return new EclipseDocumentKey(keyStr.getId());
    }

    @Override public EclipseDocumentResource getResource(ResourceKey key) {
        if(!(key instanceof EclipseDocumentKey)) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse document resource for key '" + key + "'; it is not of type EclipseDocumentKey");
        }
        final EclipseDocumentKey eclipseDocumentKey = (EclipseDocumentKey)key;
        final @Nullable EclipseDocumentResource resource = resources.get(eclipseDocumentKey.getId());
        if(resource != null) {
            return resource;
        } else {
            throw new ResourceRuntimeException("Cannot get Eclipse document resource for key '" + key + "', it does not exist");
        }
    }

    public EclipseDocumentResource getResource(String id) {
        final @Nullable EclipseDocumentResource resource = resources.get(id);
        if(resource != null) {
            return resource;
        } else {
            throw new ResourceRuntimeException("Cannot get Eclipse document resource with ID '" + id + "', it does not exist");
        }
    }

    @Override public EclipseDocumentResource getResource(ResourceKeyString keyStr) {
        if(!keyStr.qualifierMatchesOrMissing(qualifier)) {
            throw new ResourceRuntimeException("Qualifier of '" + keyStr + "' does not match qualifier '" + qualifier + "' of this resource registry");
        }
        return getResource(keyStr.getId());
    }
}
