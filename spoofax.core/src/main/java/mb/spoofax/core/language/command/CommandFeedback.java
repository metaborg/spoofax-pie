package mb.spoofax.core.language.command;

import mb.common.region.Region;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class CommandFeedback implements Serializable {
    public interface Cases<R> {
        R showFile(ResourceKey file, @Nullable Region region);

        R showText(String text, String name, @Nullable Region region);
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
