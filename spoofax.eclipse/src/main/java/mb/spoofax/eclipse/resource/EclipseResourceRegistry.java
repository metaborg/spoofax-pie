package mb.spoofax.eclipse.resource;

import mb.resource.Resource;
import mb.resource.ResourceKey;
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

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class EclipseResourceRegistry implements ResourceRegistry {
    protected static final String qualifier = "eclipse-resource";
    protected static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    static class DocumentOverride {
        final IDocument document;
        final @Nullable IFile file;

        DocumentOverride(IDocument document, @Nullable IFile file) {
            this.document = document;
            this.file = file;
        }
    }

    private final ConcurrentHashMap<String, DocumentOverride> documentOverrides = new ConcurrentHashMap<>();


    public <D extends IDocument & IDocumentExtension4> void addDocumentOverride(EclipseResourceKey key, D document, @Nullable IFile file) {
        documentOverrides.put(key.portablePathString, new DocumentOverride(document, file));
    }

    public void removeDocumentOverride(EclipseResourceKey key) {
        documentOverrides.remove(key.portablePathString);
    }

    public void clearDocumentOverrides() {
        documentOverrides.clear();
    }


    public @Nullable IResource getWrappedEclipseResource(Resource resource) {
        if(!(resource instanceof WrapsEclipseResource)) {
            throw new ResourceRuntimeException(
                "Cannot get wrapped Eclipse resource from resource '" + resource + "'; the resource does not wrap an Eclipse resource");
        }
        return ((WrapsEclipseResource) resource).getWrappedEclipseResource();
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
        final @Nullable DocumentOverride override = documentOverrides.get(portablePathString);
        if(override != null) {
            return getResource(portablePathString, (IDocument & IDocumentExtension4) override.document, override.file);
        } else {
            return getResource(portablePathString);
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


    private EclipseEclipseResource getResource(String portablePathString) {
        return getResource(Path.fromPortableString(portablePathString));
    }

    private EclipseEclipseResource getResource(IPath path) {
        final @Nullable IResource resource = root.findMember(path);
        if(resource == null) {
            throw new ResourceRuntimeException(
                "Cannot get Eclipse resource for path '" + path + "'; no resource was found");
        }
        return new EclipseEclipseResource(resource);
    }

    private <D extends IDocument & IDocumentExtension4> EclipseDocumentEclipseResource<D> getResource(String portablePathString, D document, @Nullable IFile file) {
        return new EclipseDocumentEclipseResource<>(portablePathString, document, file);
    }
}
