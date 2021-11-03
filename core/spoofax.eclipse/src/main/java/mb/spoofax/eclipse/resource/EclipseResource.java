package mb.spoofax.eclipse.resource;

import mb.resource.ResourceRuntimeException;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.HierarchicalResourceDefaults;
import mb.resource.hierarchical.HierarchicalResourceType;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.eclipse.util.ResourceUtil;
import mb.spoofax.eclipse.util.SerializableCoreException;
import mb.spoofax.eclipse.util.UncheckedCoreException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import java.util.Optional;
import java.util.stream.Stream;

public class EclipseResource extends HierarchicalResourceDefaults<EclipseResource> implements HierarchicalResource, WrapsEclipseResource {
    final EclipseResourceRegistry registry;
    final EclipseResourcePath path;
    private transient @Nullable IResource resource;
    private transient @Nullable IFile file;
    private transient @Nullable IContainer container;
    private transient @Nullable IFileStore store;


    EclipseResource(EclipseResourceRegistry registry, EclipseResourcePath path, IResource resource) {
        this.registry = registry;
        this.path = path;
        this.resource = resource;
        this.file = null;
        this.container = null;
        this.store = null;
    }

    public EclipseResource(EclipseResourceRegistry registry, EclipseResourcePath path) {
        this.registry = registry;
        this.path = path;
        this.resource = null;
        this.file = null;
        this.container = null;
        this.store = null;
    }

    public EclipseResource(EclipseResourceRegistry registry, IResource resource) {
        this.registry = registry;
        this.path = new EclipseResourcePath(resource);
        this.resource = resource;
        this.file = null;
        this.container = null;
        this.store = null;
    }

    public EclipseResource(EclipseResourceRegistry registry, IFile file) {
        this.registry = registry;
        this.path = new EclipseResourcePath(file);
        this.resource = file;
        this.file = file;
        this.container = null;
        this.store = null;
    }

    public EclipseResource(EclipseResourceRegistry registry, IContainer container) {
        this.registry = registry;
        this.path = new EclipseResourcePath(container);
        this.resource = container;
        this.file = null;
        this.container = container;
        this.store = null;
    }

    @Override public void close() {
        // Nothing to close.
    }


    public Optional<File> asLocalFile() {
        return ResourceUtil.asLocalFile(getResource());
    }

    public File toLocalFile() {
        return ResourceUtil.toLocalFile(getResource());
    }

    public Optional<FSResource> asFsResource() {
        return ResourceUtil.asFsResource(getResource());
    }

    public FSResource toFsResource() {
        return ResourceUtil.toFsResource(getResource());
    }


    @Override public EclipseResourcePath getKey() {
        return path;
    }

    @Override public EclipseResourcePath getPath() {
        return path;
    }


    @Override public boolean exists() {
        try {
            return getInfo().exists();
        } catch(IOException e) {
            return false;
        }
    }

    @Override public boolean isReadable() {
        return exists();
    }

    @Override public Instant getLastModifiedTime() throws IOException {
        final @Nullable IDocument document = getDocument();
        if(document != null) {
            return getDocumentLastModifiedTime(document);
        } else {
            final long stamp = getInfo().getLastModified();
            if(stamp == EFS.NONE) {
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
            return document.getLength() * 2L; // Java Strings are UTF-16: 2 bytes per character.
        } else {
            return getInfo().getLength();
        }
    }

    @Override public InputStream openRead() throws IOException {
        final @Nullable IDocument document = getDocument();
        if(document != null) {
            return new ByteArrayInputStream(document.get().getBytes(StandardCharsets.UTF_8));
        } else {
            try {
                return getStore().openInputStream(EFS.NONE, null);
            } catch(CoreException e) {
                throw new IOException("Creating a new input stream for resource '" + resource + "' failed unexpectedly", new SerializableCoreException(e));
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
        try {
            final IFileInfo fileInfo = getInfo();
            return !fileInfo.getAttribute(EFS.ATTRIBUTE_READ_ONLY) && !fileInfo.getAttribute(EFS.ATTRIBUTE_IMMUTABLE);
        } catch(IOException e) {
            return false;
        }
    }

    @Override public void setLastModifiedTime(Instant time) throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            final IFileInfo info = EFS.createFileInfo();
            info.setLastModified(time.toEpochMilli());
            getStore().putInfo(info, EFS.SET_LAST_MODIFIED, null);
        } catch(CoreException e) {
            throw new IOException("Setting last modified time for resource '" + resource + "' failed unexpectedly", new SerializableCoreException(e));
        }
    }

    @Override public OutputStream openWrite() throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            return getStore().openOutputStream(EFS.OVERWRITE, null);
        } catch(CoreException e) {
            throw new IOException("Creating a new output stream for resource '" + resource + "' failed unexpectedly", new SerializableCoreException(e));
        }
    }

    @Override public OutputStream openWriteAppend() throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            return getStore().openOutputStream(EFS.APPEND, null);
        } catch(CoreException e) {
            throw new IOException("Creating a new output stream for resource '" + resource + "' failed unexpectedly", new SerializableCoreException(e));
        }
    }

    @Override public OutputStream openWriteExisting() throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            return getStore().openOutputStream(EFS.OVERWRITE, null);
        } catch(CoreException e) {
            throw new IOException("Creating a new output stream for resource '" + resource + "' failed unexpectedly", new SerializableCoreException(e));
        }
    }

    @Override public OutputStream openWriteNew() throws IOException {
        // TODO: what to do when there is a document override for this resource?
        try {
            return getStore().openOutputStream(EFS.NONE, null);
        } catch(CoreException e) {
            throw new IOException("Creating a new output stream for resource '" + resource + "' failed unexpectedly", new SerializableCoreException(e));
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

    @Override public EclipseResource getNormalized() {
        final EclipseResourcePath newPath = path.getNormalized();
        return new EclipseResource(registry, newPath);
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

    @Override public EclipseResource appendString(String other) {
        return new EclipseResource(registry, path.appendString(other));
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


    @Override public Stream<EclipseResource> list() throws IOException {
        try {
            final IContainer container = getContainer();
            // HACK: refresh before listing to ensure that Eclipse sees new resources.
            container.refreshLocal(IResource.DEPTH_ONE, null);
            final IResource[] members = container.members();
            return Arrays.stream(members).map(m -> new EclipseResource(registry, m));
        } catch(CoreException e) {
            throw new IOException("Listing resources in '" + path + "' failed unexpectedly", new SerializableCoreException(e));
        }
    }

    @Override public Stream<EclipseResource> list(ResourceMatcher matcher) throws IOException {
        try {
            final IContainer container = getContainer();
            // HACK: refresh before listing to ensure that Eclipse sees new resources.
            container.refreshLocal(IResource.DEPTH_ONE, null);
            final IResource[] members = container.members();
            return Arrays.stream(members).map(m -> new EclipseResource(registry, m)).filter((r) -> {
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
                "Listing resources in '" + path + "' with matcher '" + matcher + "' failed unexpectedly", new SerializableCoreException(e));
        }
    }

    @Override public Stream<EclipseResource> walk() throws IOException {
        final Stream.Builder<EclipseResource> builder = Stream.builder();
        try {
            final IContainer container = getContainer();
            // HACK: refresh before walking to ensure that Eclipse sees new resources.
            container.refreshLocal(IResource.DEPTH_INFINITE, null);
            recursiveMembers(container, builder, null, null);
        } catch(CoreException e) {
            throw new IOException("Walking resources in '" + path + "' failed unexpectedly", new SerializableCoreException(e));
        }
        return builder.build();
    }

    @Override public Stream<EclipseResource> walk(ResourceWalker walker, ResourceMatcher matcher) throws IOException {
        final Stream.Builder<EclipseResource> builder = Stream.builder();
        try {
            final IContainer container = getContainer();
            // HACK: refresh before walking to ensure that Eclipse sees new resources.
            container.refreshLocal(IResource.DEPTH_INFINITE, null);
            recursiveMembers(container, builder, walker, matcher);
        } catch(CoreException e) {
            throw new IOException("Walking resources in '" + path + "' with walker '" + walker + "' and matcher '" + matcher + "' failed unexpectedly", new SerializableCoreException(e));
        }
        return builder.build();
    }

    private void recursiveMembers(IContainer container, Stream.Builder<EclipseResource> builder, @Nullable ResourceWalker walker, @Nullable ResourceMatcher matcher) throws CoreException, IOException {
        final IResource[] members = container.members();
        for(IResource member : members) {
            final EclipseResource resource = new EclipseResource(registry, member);
            if(matcher == null || matcher.matches(resource, this)) {
                builder.accept(resource);
            }
            if(member instanceof IContainer) {
                if(walker == null || walker.traverse(resource, this)) {
                    // OPTO: non-recursive implementation.
                    recursiveMembers((IContainer)member, builder, walker, matcher);
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
            throw new IOException("Copying from '" + path + "' to '" + other.path + "' failed unexpectedly", new SerializableCoreException(e));
        }
    }

    @Override public void copyRecursivelyTo(HierarchicalResource other) throws IOException {
        if(!(other instanceof EclipseResource)) {
            throw new ResourceRuntimeException("Cannot copy to '" + other + "', it is not an EclipseResource");
        }
        copyRecursivelyTo((EclipseResource)other);
    }

    public void copyRecursivelyTo(EclipseResource other) throws IOException {
        final IWorkspaceRoot root = getWorkspaceRoot();
        // Normalize target directory to ensure that unpacked files have the target directory as prefix.
        final EclipseResource targetDirectory = other.getNormalized();
        final IPath targetDirectoryEclipsePath = targetDirectory.getPath().path;
        try {
            try(Stream<EclipseResource> stream = this.walk()) {
                stream.forEachOrdered(source -> {
                    try {
                        final IPath relativePath = source.getPath().path.makeRelativeTo(this.getPath().path);
                        final IPath target = targetDirectoryEclipsePath.append(relativePath);
                        if(!targetDirectoryEclipsePath.isPrefixOf(target)) {
                            throw new IOException("Cannot copy '" + relativePath + "' from '" + this + "', resulting path '" + target + "' is not in the target directory '" + other + "'");
                        }
                        if(!root.exists(target)) {
                            switch(source.getType()) {
                                case File:
                                    source.getFile().copy(target, true, null);
                                    break;
                                case Directory:
                                    root.getFolder(target).create(true, true, null);
                                    break;
                                case Unknown:
                                    break;
                            }
                        }
                    } catch(IOException e) {
                        throw new UncheckedIOException(e);
                    } catch(CoreException e) {
                        throw new UncheckedCoreException(e);
                    }
                });
            }
        } catch(UncheckedIOException e) {
            throw e.getCause();
        } catch(UncheckedCoreException e) {
            throw new IOException(new SerializableCoreException(e.getCause()));
        }
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
            throw new IOException("Moving from '" + path + "' to '" + other.path + "' failed unexpectedly", new SerializableCoreException(e));
        }
    }


    @Override public EclipseResource createFile(boolean createParents) throws IOException {
        if(createParents) {
            createParents();
        }
        try {
            getFile().create(new ByteArrayInputStream(new byte[0]), true, null);
        } catch(CoreException e) {
            final int code = e.getStatus().getCode();
            if(code == IResourceStatus.PATH_OCCUPIED || code == IResourceStatus.RESOURCE_EXISTS || code == IResourceStatus.CASE_VARIANT_EXISTS) {
                throw new FileAlreadyExistsException("The resource already exists: " + path);
            } else {
                throw new IOException("Creating file '" + path + "' failed unexpectedly", new SerializableCoreException(e));
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
            throw new IOException("Creating directory '" + container + "' failed unexpectedly", new SerializableCoreException(e));
        }
        throw new IOException("Cannot create directory '" + container + "', it is not an IFolder nor an IProject");
    }


    @Override public void delete(boolean deleteContents) throws IOException {
        try {
            getResource().delete(true, null);
        } catch(CoreException e) {
            throw new IOException("Deleting '" + path + "' failed unexpectedly", new SerializableCoreException(e));
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

    private IFileStore getStore() throws IOException {
        if(store != null) {
            return store;
        }
        try {
            store = EFS.getStore(getResource().getLocationURI());
            return store;
        } catch(CoreException e) {
            throw new IOException("Getting file store for resource '" + resource + "' failed unexpectedly", new SerializableCoreException(e));
        }
    }

    private IFileInfo getInfo() throws IOException {
        return getStore().fetchInfo();
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
