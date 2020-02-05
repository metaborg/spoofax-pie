package mb.common.message;

import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface KeyedMessageVisitor {
    boolean regionOrigin(String text, @Nullable Throwable exception, Severity severity, ResourceKey resource, Region region);

    boolean resourceOrigin(String text, @Nullable Throwable exception, Severity severity, ResourceKey resource);

    boolean noOrigin(String text, @Nullable Throwable exception, Severity severity);
}
