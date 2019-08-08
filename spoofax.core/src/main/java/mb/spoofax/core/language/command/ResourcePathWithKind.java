package mb.spoofax.core.language.command;

import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class ResourcePathWithKind implements Serializable {
    interface Cases<R> {
        R project(ResourcePath path);

        R directory(ResourcePath path);

        R file(ResourcePath path);
    }

    public abstract <R> R match(Cases<R> cases);

    public ResourcePath getPath() {
        return ResourcePathWithKinds.getPath(this);
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
