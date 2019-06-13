package mb.spoofax.eclipse.resource;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.Resource;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class EclipseResourceRegistry implements ResourceRegistry {
    static final String qualifier = "eclipse-resource";
    static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    private static class DocumentOverride {
        final IDocument document;
        final @Nullable IFile file;

        DocumentOverride(IDocument document, @Nullable IFile file) {
            this.document = document;
            this.file = file;
        }
    }

    private final Logger logger;
    private final ConcurrentHashMap<String, DocumentOverride> documentOverrides = new ConcurrentHashMap<>();


    @Inject public EclipseResourceRegistry(LoggerFactory loggerFactory) {
        this.logger = loggerFactory.create(getClass());
    }


    public <D extends IDocument & IDocumentExtension4> void addDocumentOverride(EclipseResourceKey key, D document, @Nullable IFile file) {
        logger.trace("Overriding resource '{}' with document '{}'", key.portablePathString, document);
        documentOverrides.put(key.portablePathString, new DocumentOverride(document, file));
    }

    public void removeDocumentOverride(EclipseResourceKey key) {
        logger.trace("Removing document override for resource '{}'", key.portablePathString);
        documentOverrides.remove(key.portablePathString);
    }

    public void clearDocumentOverrides() {
        logger.trace("Clearing all document overrides");
        documentOverrides.clear();
    }


    public @Nullable IResource getWrappedEclipseResource(Resource resource) {
        if(!(resource instanceof WrapsEclipseResource)) {
            throw new ResourceRuntimeException(
                "Cannot get wrapped Eclipse resource from resource '" + resource + "'; the resource does not wrap an Eclipse resource");
        }
        return ((WrapsEclipseResource) resource).getWrappedEclipseResource();
    }


    @Override public Resource getResource(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse resource with ID '" + id + "'; the ID is not of type String");
        }
        final String portablePathString = (String) id;
        final @Nullable DocumentOverride override = documentOverrides.get(portablePathString);
        if(override != null) {
            return getResource(portablePathString, (IDocument & IDocumentExtension4) override.document, override.file);
        } else {
            return getResource(portablePathString);
        }
    }

    @Override public EclipseResource getResource(String idStr) {
        return getResource(Path.fromPortableString(idStr));
    }

    @Override public String qualifier() {
        return qualifier;
    }

    @Override public String toStringRepresentation(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot convert identifier '" + id + "' to its string representation; it is not of type String");
        }
        return (String) id;
    }


    private EclipseResource getResource(IPath path) {
        final @Nullable IResource resource = root.findMember(path);
        if(resource == null) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse resource for path '" + path + "'; no resource was found");
        }
        return new EclipseResource(resource);
    }

    private <D extends IDocument & IDocumentExtension4> EclipseDocumentEclipseResource<D> getResource(String portablePathString, D document, @Nullable IFile file) {
        return new EclipseDocumentEclipseResource<>(portablePathString, document, file);
    }
}
