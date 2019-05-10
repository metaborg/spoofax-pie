package mb.spoofax.eclipse.resource;

import mb.resource.Resource;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class EclipseResourceRegistry implements ResourceRegistry {
    protected static final String qualifier = "eclipse-resource";
    protected static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    private final ConcurrentHashMap<String, IDocument> documentOverrides = new ConcurrentHashMap<>();


    public <D extends IDocument & IDocumentExtension4> void addDocumentOverride(EclipseResourceKey key, D document) {
        documentOverrides.put(key.portablePathString, document);
    }

    public void removeDocumentOverride(EclipseResourceKey key) {
        documentOverrides.remove(key.portablePathString);
    }

    public void clearDocumentOverrides() {
        documentOverrides.clear();
    }


    public EclipseResource getResource(String portablePathString) {
        return getResource(Path.fromPortableString(portablePathString));
    }

    public EclipseResource getResource(IPath path) {
        final @Nullable IResource resource = root.findMember(path);
        if(resource == null) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse resource for path '" + path + "'; no resource was found");
        }
        return new EclipseResource(resource);
    }

    public <D extends IDocument & IDocumentExtension4> EclipseDocumentResource<D> getResource(String portablePathString, D document) {
        return new EclipseDocumentResource<>(portablePathString, document);
    }


    @Override public Serializable qualifier() {
        return qualifier;
    }

    @Override public Resource getResource(Serializable id) {
        if(!(id instanceof String)) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse resource with ID '" + id + "'; the ID is not of type String");
        }
        final String portablePathString = (String) id;
        final @Nullable IDocument document = documentOverrides.get(portablePathString);
        if(document != null) {
            return getResource(portablePathString, (IDocument & IDocumentExtension4) document);
        } else {
            return getResource(portablePathString);
        }
    }
}
