package mb.common.message;

import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MessageVisitor {
    boolean regionOrigin(String text, @Nullable Throwable exception, Severity severity, ResourceKey key, Region region);

    boolean resourceOrigin(String text, @Nullable Throwable exception, Severity severity, ResourceKey key);

    boolean noOrigin(String text, @Nullable Throwable exception, Severity severity);
}
