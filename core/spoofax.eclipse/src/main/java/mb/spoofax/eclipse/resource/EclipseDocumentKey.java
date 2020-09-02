package mb.spoofax.eclipse.resource;

import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;

import java.util.Objects;

public class EclipseDocumentKey implements ResourceKey {
    private final String id;

    public EclipseDocumentKey(String id) {
        this.id = id;
    }

    public EclipseDocumentKey(IFile file) {
        this.id = file.getFullPath().toPortableString();
    }


    @Override public String getQualifier() {
        return EclipseDocumentResourceRegistry.qualifier;
    }

    @Override public String getId() {
        return id;
    }

    @Override public String getIdAsString() {
        return id;
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final EclipseDocumentKey other = (EclipseDocumentKey)obj;
        return id.equals(other.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }

    @Override public String toString() {
        return asString();
    }
}
