package mb.spoofax.core.language.transform;

import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public abstract class ResourceSubject implements TransformSubject {
    protected final ResourceKey resourceKey;

    protected ResourceSubject(ResourceKey resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final ResourceSubject other = (ResourceSubject) obj;
        return resourceKey.equals(other.resourceKey);
    }

    @Override public int hashCode() {
        return Objects.hash(resourceKey);
    }

    @Override public String toString() {
        return resourceKey.toString();
    }
}
