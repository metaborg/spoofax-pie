package mb.spoofax.core.language.transform;

import mb.common.region.Region;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class TransformContext implements Serializable {
    interface Cases<R> {
        R project(ResourcePath project);

        R directory(ResourcePath directory);

        R file(ResourcePath file);

        R fileWithRegion(ResourcePath file, Region region);

        R fileWithOffset(ResourcePath file, int offset);

        R editor(ResourceKey readable);

        R editorWithRegion(ResourceKey readable, Region region);

        R editorWithOffset(ResourceKey readable, int offset);

        R none();
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
