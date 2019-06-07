package mb.spoofax.eclipse.resource;

import mb.resource.ResourceKey;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import java.io.Serializable;

public class EclipseResourceKey implements ResourceKey {
    final String portablePathString;


    public EclipseResourceKey(String portablePathString) {
        this.portablePathString = portablePathString;
    }

    public EclipseResourceKey(IPath path) {
        this(path.toPortableString());
    }

    public EclipseResourceKey(IResource resource) {
        this(resource.getFullPath().toPortableString());
    }


    @Override public String getQualifier() {
        return EclipseResourceRegistry.qualifier;
    }

    @Override public String getId() {
        return portablePathString;
    }

    @Override public String getIdStringRepresentation() {
        return portablePathString;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final EclipseResourceKey that = (EclipseResourceKey) o;
        return portablePathString.equals(that.portablePathString);
    }

    @Override public int hashCode() {
        return portablePathString.hashCode();
    }

    @Override public String toString() {
        return ResourceKey.toStringRepresentation(this);
    }
}
