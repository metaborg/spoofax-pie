package mb.common.message;

import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
public interface GeneralMessageVisitor {
    boolean message(String text, @Nullable Throwable exception, Severity severity, @Nullable ResourceKey resource, @Nullable Region region);
}
