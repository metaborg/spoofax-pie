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

    public static ResourcePathWithKind project(ResourcePath path) {
        return ResourcePathWithKinds.project(path);
    }

    public static ResourcePathWithKind directory(ResourcePath path) {
        return ResourcePathWithKinds.directory(path);
    }

    public static ResourcePathWithKind file(ResourcePath path) {
        return ResourcePathWithKinds.file(path);
    }


    public abstract <R> R match(Cases<R> cases);

    public ResourcePath getPath() {
        return ResourcePathWithKinds.getPath(this);
    }

    public ResourcePathWithKinds.CaseOfMatchers.TotalMatcher_Project caseOf() {
        return ResourcePathWithKinds.caseOf(this);
    }

    public String toDisplayString() {
        return caseOf()
            .project(p -> "project '" + p + "'")
            .directory(d -> "directory '" + d + "'")
            .file(f -> "file '" + f + "");
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
