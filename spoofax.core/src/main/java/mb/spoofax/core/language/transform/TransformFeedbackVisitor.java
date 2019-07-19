package mb.spoofax.core.language.transform;

import mb.common.message.Messages;
import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface TransformFeedbackVisitor {
    void messages(Messages messages);

    void openEditor(ResourceKey file, @Nullable Region region);

    void openEditor(String text, String name, @Nullable Region region);
}
