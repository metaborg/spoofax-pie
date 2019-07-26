package mb.spoofax.core.language.transform;

import mb.common.message.Messages;
import mb.common.region.Region;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class TransformFeedback implements Serializable {
    public interface Cases<R> {
        R openEditorForFile(ResourceKey file, @Nullable Region region);

        R openEditorWithText(String text, String name, @Nullable Region region);
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
