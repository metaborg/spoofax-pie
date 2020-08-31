package mb.spoofax.eclipse.resource;

import mb.resource.ResourceRuntimeException;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.HierarchicalResourceAccess;
import mb.resource.hierarchical.HierarchicalResourceDefaults;
import mb.resource.hierarchical.HierarchicalResourceType;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.stream.Stream;

import static org.eclipse.core.resources.IResourceStatus.RESOURCE_EXISTS;

public class EclipseResource extends HierarchicalResourceDefaults<EclipseResource> implements HierarchicalResource, WrapsEclipseResource {
    final EclipseResourceRegistry registry;
    final EclipseResourcePath path;
    private transient @Nullable IResource resource;
    private transient @Nullable IFile file;
    private transient @Nullable IContainer container;


    EclipseResource(EclipseResourceRegistry registry, EclipseResourcePath path, IResource resource) {
        this.registry = registry;
        this.path = path;
        this.resource = resource;
        this.file = null;
        this.container = null;
    }

    public EclipseResource(EclipseResourceRegistry registry, EclipseResourcePath path) {
        this.registry = registry;
        this.path = path;
        this.resource = null;
        this.file = null;
        this.container = null;
    }

    public EclipseResource(EclipseResourceRegistry registry, IResource resource) {
        this.registry = registry;
        this.path = new EclipseResourcePath(resource);
        this.resource = resource;
        this.file = null;
        this.container = null;
    }

    public EclipseResource(EclipseResourceRegistry registry, IFile file) {
        this.registry = registry;
        this.path = new EclipseResourcePath(file);
        this.resource = file;
        this.file = file;
        this.container = null;
    }

    public EclipseResource(EclipseResourceRegistry registry, IContainer container) {
        this.registry = registry;
        this.path = new EclipseResourcePath(container);
        this.resource = container;
        this.file = null;
        this.container = container;
    }

    @Override public void close() {
        // Nothing to close.
    }


    @Override public EclipseResourcePath getKey() {
        return path;
    }

    @Override public EclipseResourcePath getPath() {
        return path;
    }


    @Override public boolean exists() {
        return getResource().exists();
    }

    @Override public boolean isReadable() {
        return getResource().isAccessible();
    }

    @Override public Instant getLastModifiedTime() {
        final @Nullable IDocument document = getDocument();
        if(document != null) {
            return getDocumentLastModifiedTime(document);
        } else {
            final long stamp = getResource().getModificationStamp();
            if(stamp == IResource.NULL_STAMP) {
                return Instant.MIN;
            }
            return Instant.ofEpochMilli(stamp);
        }
    }

    private Instant getDocumentLastModifiedTime(IDocument document) {
        if(document instanceof IDocumentExtension4) {
            final IDocumentExtension4 documentExtension = (IDocumentExtension4)document;
            final long stamp = documentExtension.getModificationStamp();
            if(stamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
                return Instant.MIN;
            }
            return Instant.ofEpochMilli(stamp);
        } else {
            return Instant.MAX;
        }
    }

    @Override public long getSize() throws IOException {
        final @Nullable IDocument document = getDocument();
        if(document != null) {
            return document.getLength() * 2; // Java Strings are UTF-16: 2 bytes per character.
        } else {
            return getFileStore().fetchInfo().getLength();
        }
    }

    @Override public InputStream openRead() throws IOException {
        final @Nullable IDocument document = getDocument();
        if(document != null) {
            return new ByteArrayInputStream(document.get().getBytes(StandardCharsets.UTF_8));
        } else {
            try {
                return getFileStore().openInputStream(EFS.NONE, null);
            } catch(CoreException e) {
                throw new IOException("Creating a new input stream for resource '" + resource + "' failed unexpectedly", e);
            }
        }
    }

    @Override public String readString(Charset fromCharset) throws IOException {
        final @Nullable IDocument document = getDocument();
        if(document != null) {
            return document.get(); // Ignore the character set, we do not need to decode from bytes.
        } else {
            return super.readString(fromCharset);
        }
    }

    @Override public boolean isWritable() {
        // TODO: what to do when there is a document override for this resource?
        final @Nullable ResourceAttributes resourceAttributes = getResource().getResourceAttributes();
        if(resourceAttributes == null) {
            return false;
        }
        return !resourceAttributes.isReadOnly();
    }

    @Override public void setLastModifiedTime(Instant time) throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            getResource().revertModificationStamp(time.toEpochMilli());
        } catch(CoreException e) {
            throw new IOException("Setting last modified time for resource '" + resource + "' failed unexpectedly", e);
        }
    }

    @Override public OutputStream openWrite() throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            return getFileStore().openOutputStream(EFS.OVERWRITE, null);
        } catch(CoreException e) {
            throw new IOException("Creating a new output stream for resource '" + resource + "' failed unexpectedly", e);
        }
    }

    @Override public OutputStream openWriteAppend() throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            return getFileStore().openOutputStream(EFS.APPEND, null);
        } catch(CoreException e) {
            throw new IOException("Creating a new output stream for resource '" + resource + "' failed unexpectedly", e);
        }
    }

    @Override public OutputStream openWriteExisting() throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            return getFileStore().openOutputStream(EFS.OVERWRITE, null);
        } catch(CoreException e) {
            throw new IOException("Creating a new output stream for resource '" + resource + "' failed unexpectedly", e);
        }
    }

    @Override public OutputStream openWriteNew() throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            return getFileStore().openOutputStream(EFS.NONE, null);
        } catch(CoreException e) {
            throw new IOException("Creating a new output stream for resource '" + resource + "' failed unexpectedly", e);
        }
    }


    @Override public @Nullable EclipseResource getParent() {
        final @Nullable EclipseResourcePath newPath = path.getParent();
        if(newPath == null) {
            return null;
        }
        return new EclipseResource(registry, newPath);
    }

    @Override public @Nullable EclipseResource getRoot() {
        return new EclipseResource(registry, getWorkspaceRoot());
    }

    private IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }


    @Override public EclipseResource appendSegment(String segment) {
        return new EclipseResource(registry, path.appendSegment(segment));
    }

    @Override public EclipseResource appendSegments(Iterable<String> segments) {
        return new EclipseResource(registry, path.appendSegments(segments));
    }

    @Override public EclipseResource appendSegments(Collection<String> segments) {
        return new EclipseResource(registry, path.appendSegments(segments));
    }


    @Override public EclipseResource appendRelativePath(String relativePath) {
        return new EclipseResource(registry, path.appendRelativePath(relativePath));
    }

    @Override public EclipseResource appendOrReplaceWithPath(String other) {
        return new EclipseResource(registry, path.appendOrReplaceWithPath(other));
    }

    @Override public EclipseResource appendRelativePath(ResourcePath relativePath) {
        return new EclipseResource(registry, path.appendRelativePath(relativePath));
    }


    @Override public EclipseResource replaceLeaf(String segment) {
        return new EclipseResource(registry, path.replaceLeaf(segment));
    }


    @Override public HierarchicalResourceType getType() {
        switch(getResource().getType()) {
            case IResource.FILE:
                return HierarchicalResourceType.File;
            case IResource.FOLDER:
            case IResource.ROOT:
            case IResource.PROJECT:
                return HierarchicalResourceType.Directory;
            default:
                return HierarchicalResourceType.Unknown;
        }
    }


    @Override public Stream<? extends EclipseResource> list() throws IOException {
        try {
            final IResource[] members = getContainer().members();
            return Arrays.stream(members).map(resource1 -> new EclipseResource(registry, resource1));
        } catch(CoreException e) {
            throw new IOException("Listing resources in '" + path + "' failed unexpectedly", e);
        }
    }

    @Override public Stream<? extends EclipseResource> list(ResourceMatcher matcher) throws IOException {
        try {
            final IResource[] members = getContainer().members();
            return Arrays.stream(members).map(resource1 -> new EclipseResource(registry, resource1)).filter((r) -> {
                try {
                    return matcher.matches(r, this);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        } catch(CoreException e) {
            throw new IOException(
                "Listing resources in '" + path + "' with matcher '" + matcher + "' failed unexpectedly", e);
        }
    }

    @Override public Stream<? extends EclipseResource> walk() throws IOException {
        final Stream.Builder<EclipseResource> builder = Stream.builder();
        try {
            recursiveMembers(getContainer(), builder, null, null, null);
        } catch(CoreException e) {
            throw new IOException("Walking resources in '" + path + "' failed unexpectedly", e);
        }
        return builder.build();
    }

    @Override
    public Stream<? extends EclipseResource> walk(ResourceWalker walker, ResourceMatcher matcher, @Nullable HierarchicalResourceAccess access) throws IOException {
        final Stream.Builder<EclipseResource> builder = Stream.builder();
        try {
            recursiveMembers(getContainer(), builder, walker, matcher, access);
        } catch(CoreException e) {
            throw new IOException(
                "Walking resources in '" + path + "' with walker '" + walker + "' and matcher '" + matcher + "' failed unexpectedly",
                e);
        }
        return builder.build();
    }

    private void recursiveMembers(IContainer container, Stream.Builder<EclipseResource> builder, @Nullable ResourceWalker walker, @Nullable ResourceMatcher matcher, @Nullable HierarchicalResourceAccess access) throws CoreException, IOException {
        final IResource[] members = container.members();
        for(IResource member : members) {
            final EclipseResource resource = new EclipseResource(registry, member);
            if(access != null) {
                access.read(resource);
            }
            if(matcher == null || matcher.matches(resource, this)) {
                builder.accept(resource);
            }
            if(member instanceof IContainer) {
                if(walker == null || walker.traverse(resource, this)) {
                    // OPTO: non-recursive implementation.
                    recursiveMembers((IContainer)member, builder, walker, matcher, access);
                }
            }
        }
    }


    @Override public void copyTo(HierarchicalResource other) throws IOException {
        if(!(other instanceof EclipseResource)) {
            throw new ResourceRuntimeException("Cannot copy to '" + other + "', it is not an EclipseResource");
        }
        copyTo((EclipseResource)other);
    }

    public void copyTo(EclipseResource other) throws IOException {
        try {
            getResource().copy(other.path.path, true, null);
        } catch(CoreException e) {
            throw new IOException("Copying from '" + path + "' to '" + other.path + "' failed unexpectedly", e);
        }
    }

    @Override public void copyRecursivelyTo(HierarchicalResource other) throws IOException {
        if(!(other instanceof EclipseResource)) {
            throw new ResourceRuntimeException("Cannot copy to '" + other + "', it is not an EclipseResource");
        }
        copyRecursivelyTo((EclipseResource)other);
    }

    public void copyRecursivelyTo(EclipseResource other) throws IOException {
        copyTo(other);
    }

    @Override public void moveTo(HierarchicalResource other) throws IOException {
        if(!(other instanceof EclipseResource)) {
            throw new ResourceRuntimeException("Cannot move to '" + other + "', it is not an EclipseResource");
        }
        moveTo((EclipseResource)other);
    }

    public void moveTo(EclipseResource other) throws IOException {
        try {
            getResource().move(other.path.path, true, null);
        } catch(CoreException e) {
            throw new IOException("Moving from '" + path + "' to '" + other.path + "' failed unexpectedly", e);
        }
    }


    @Override public EclipseResource createFile(boolean createParents) throws IOException {
        if(createParents) {
            createParents();
        }
        try {
            getFile().create(new ByteArrayInputStream(new byte[0]), true, null);
        } catch(CoreException e) {
            if(e.getStatus().getCode() == RESOURCE_EXISTS) {
                throw new FileAlreadyExistsException("The resource already exists: " + path);
            } else {
                throw new IOException("Creating file '" + path + "' failed unexpectedly", e);
            }
        }
        return this;
    }

    @Override public EclipseResource createDirectory(boolean createParents) throws IOException {
        if(path.getSegmentCount() == 0) {
            throw new IOException("Cannot create directory '" + path + "', it is the workspace root");
        }
        if(createParents) {
            createParents();
        }
        createDirectory(getContainer());
        return this;
    }

    @Override public EclipseResource createParents() throws IOException {
        final Deque<IContainer> containers = new ArrayDeque<>();
        IContainer parent = getResource().getParent();
        while(parent != null) {
            containers.push(parent);
            parent = parent.getParent();
        }
        for(IContainer container : containers) {
            createDirectory(container);
        }
        return this;
    }

    private void createDirectory(IContainer container) throws IOException {
        try {
            if(container instanceof IFolder) {
                final IFolder folder = (IFolder)container;
                if(!folder.exists()) {
                    folder.create(true, true, null);
                }
                return;
            } else if(container instanceof IProject) {
                final IProject project = (IProject)container;
                if(!project.exists()) {
                    project.create(null);
                }
                return;
            } else if(container instanceof IWorkspaceRoot) {
                return; // Ignore; workspace root always exists.
            }
        } catch(CoreException e) {
            throw new IOException("Creating directory '" + container + "' failed unexpectedly", e);
        }
        throw new IOException("Cannot create directory '" + container + "', it is not an IFolder nor an IProject");
    }


    @Override public void delete(boolean deleteContents) throws IOException {
        try {
            getResource().delete(true, null);
        } catch(CoreException e) {
            throw new IOException("Deleting '" + path + "' failed unexpectedly", e);
        }
    }


    private IResource getResource() {
        if(resource != null) {
            return resource;
        }
        final IWorkspaceRoot root = getWorkspaceRoot();
        final IPath p = this.path.path;
        resource = root.findMember(p);
        if(resource == null) {
            if(!p.hasTrailingSeparator()) {
                resource = root.getFile(p);
            } else {
                resource = root.getFolder(p);
            }
        }
        return resource;
    }

    private IFile getFile() {
        if(file != null) {
            return file;
        }
        if(resource != null && resource instanceof IFile) {
            file = (IFile)resource;
            return file;
        }
        file = getWorkspaceRoot().getFile(path.path);
        return file;
    }

    private IContainer getContainer() {
        if(container != null) {
            return container;
        }
        if(resource != null && resource instanceof IContainer) {
            container = (IContainer)resource;
            return container;
        }
        final int segmentCount = path.path.segmentCount();
        final IWorkspaceRoot root = getWorkspaceRoot();
        switch(segmentCount) {
            case 0:
                container = root;
                break;
            case 1:
                container = root.getProject(path.path.lastSegment());
                break;
            default:
                container = root.getFolder(path.path);
                break;
        }
        return container;
    }

    private IFileStore getFileStore() throws IOException {
        try {
            return EFS.getStore(getFile().getLocationURI());
        } catch(CoreException e) {
            throw new IOException("Getting file store for resource '" + resource + "' failed unexpectedly", e);
        }
    }

    private @Nullable IDocument getDocument() {
        return registry.getDocumentOverride(path);
    }


    @Override public IResource getWrappedEclipseResource() {
        return getResource();
    }


    @Override protected EclipseResource self() {
        return this;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final EclipseResource that = (EclipseResource)o;
        return path.equals(that.path);
    }

    @Override public int hashCode() {
        return path.hashCode();
    }

    @Override public String toString() {
        return path.toString();
    }
}
