package mb.spoofax.eclipse.resource;

import mb.resource.ResourceRuntimeException;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.ResourcePathDefaults;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;

public class EclipseResourcePath extends ResourcePathDefaults<EclipseResourcePath> implements ResourcePath {
    // String version of the path which can be serialized and deserialized.
    final String pathString;
    // Transient and non-final for deserialization in readObject. Invariant: always nonnull.
    transient IPath path;


    EclipseResourcePath(String pathString, IPath path) {
        this.pathString = pathString;
        this.path = path;
    }

    public EclipseResourcePath(IPath path) {
        this(path.toPortableString(), path);
    }

    public EclipseResourcePath(String portablePathString) {
        this(portablePathString, Path.fromPortableString(portablePathString));
    }

    public EclipseResourcePath(IResource resource) {
        this(resource.getFullPath());
    }


    public IPath getEclipsePath() {
        return path;
    }


    @Override public String getQualifier() {
        return EclipseResourceRegistry.qualifier;
    }

    @Override public String getId() {
        return pathString;
    }

    @Override public String getIdAsString() {
        return pathString;
    }


    @Override public boolean isAbsolute() {
        return path.isAbsolute();
    }


    @Override public int getSegmentCount() {
        return path.segmentCount();
    }

    @Override public Iterable<String> getSegments() {
        return Arrays.asList(path.segments());
    }


    @Override public boolean startsWith(ResourcePath prefix) {
        if(!(prefix instanceof EclipseResourcePath)) {
            throw new ResourceRuntimeException("Cannot check if this path starts with '" + prefix + "', it is not an EclipseResourcePath");
        }
        return startsWith((EclipseResourcePath)prefix);
    }

    public boolean startsWith(EclipseResourcePath prefix) {
        return prefix.path.isPrefixOf(path);
    }


    @Override public @Nullable EclipseResourcePath getParent() {
        if(path.segmentCount() == 0) {
            return null;
        }
        return new EclipseResourcePath(path.uptoSegment(path.segmentCount() - 1));
    }

    @Override public @Nullable EclipseResourcePath getRoot() {
        return new EclipseResourcePath(path.uptoSegment(0));
    }

    @Override public @Nullable String getLeaf() {
        return path.lastSegment();
    }

    @Override public @Nullable String getLeafExtension() {
        return path.getFileExtension();
    }

    @Override public EclipseResourcePath getNormalized() {
        return this;
    }

    @Override public String relativize(ResourcePath other) {
        if(!(other instanceof EclipseResourcePath)) {
            throw new ResourceRuntimeException(
                "Cannot relativize against '" + other + "', it is not an EclipseResourceKey");
        }
        return relativize((EclipseResourcePath)other);
    }

    public String relativize(EclipseResourcePath other) {
        return path.makeRelativeTo(other.path).toPortableString();
    }


    @Override public EclipseResourcePath appendSegment(String segment) {
        return new EclipseResourcePath(path.append(segment));
    }

    @Override public EclipseResourcePath appendSegments(Iterable<String> segments) {
        IPath appendedPath = path;
        for(String segment : segments) {
            appendedPath = appendedPath.append(segment);
        }
        return new EclipseResourcePath(appendedPath);
    }

    @Override public EclipseResourcePath appendSegments(Collection<String> segments) {
        IPath appendedPath = path;
        for(String segment : segments) {
            appendedPath = appendedPath.append(segment);
        }
        return new EclipseResourcePath(appendedPath);
    }


    @Override public EclipseResourcePath appendRelativePath(String relativePath) {
        return new EclipseResourcePath(appendRelativePath(Path.fromPortableString(relativePath)));
    }

    private IPath appendRelativePath(IPath relativePath) {
        if(relativePath.isAbsolute()) {
            throw new ResourceRuntimeException("Cannot append path '" + relativePath + "', it is an absolute path");
        }
        return path.append(relativePath);
    }

    @Override public EclipseResourcePath appendOrReplaceWithPath(String other) {
        return new EclipseResourcePath(appendOrReplaceWithPath(Path.fromPortableString(other)));
    }

    @Override public EclipseResourcePath appendString(String other) {
        return new EclipseResourcePath(path.append(other));
    }

    private IPath appendOrReplaceWithPath(IPath other) {
        if(other.isAbsolute()) {
            return other;
        }
        return path.append(other);
    }

    @Override public EclipseResourcePath appendRelativePath(ResourcePath relativePath) {
        if(!(relativePath instanceof EclipseResourcePath)) {
            throw new ResourceRuntimeException(
                "Cannot append '" + relativePath + "', it is not an EclipseResourcePath");
        }
        return appendRelativePath((EclipseResourcePath)relativePath);
    }

    public EclipseResourcePath appendRelativePath(EclipseResourcePath relativePath) {
        return new EclipseResourcePath(appendRelativePath(relativePath.path));
    }


    @Override public EclipseResourcePath replaceLeaf(String segment) {
        if(path.segmentCount() == 0) {
            return this;
        }
        return new EclipseResourcePath(path.removeLastSegments(1).append(segment));
    }


    @Override protected EclipseResourcePath self() {
        return this;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final EclipseResourcePath that = (EclipseResourcePath)o;
        return pathString.equals(that.pathString);
    }

    @Override public int hashCode() {
        return pathString.hashCode();
    }

    @Override public String toString() {
        return asString();
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        this.path = Path.fromPortableString(pathString);
    }
}
