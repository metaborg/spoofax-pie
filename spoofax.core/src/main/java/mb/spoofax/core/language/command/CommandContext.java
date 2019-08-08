package mb.spoofax.core.language.command;

import mb.common.region.Region;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class CommandContext implements Serializable {
    interface Cases<R> {
        R project(ResourcePath project);

        R directory(ResourcePath directory);

        R file(ResourcePath file);

        R fileWithRegion(ResourcePath file, Region region);

        R fileWithOffset(ResourcePath file, int offset);

        R textResource(ResourceKey readable);

        R textResourceWithRegion(ResourceKey readable, Region region);

        R textResourceWithOffset(ResourceKey readable, int offset);

        R none();
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
