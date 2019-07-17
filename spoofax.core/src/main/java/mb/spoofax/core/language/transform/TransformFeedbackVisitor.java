package mb.spoofax.core.language.transform;

import mb.common.message.Messages;
import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface TransformFeedbackVisitor {
    void error(Throwable error);

    void messages(Messages messages);

    void openEditor(ResourceKey file, @Nullable Region region);

    void openEditor(String text, @Nullable Region region);
}
