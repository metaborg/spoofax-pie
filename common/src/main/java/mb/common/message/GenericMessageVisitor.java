package mb.common.message;

import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
public interface GenericMessageVisitor {
    boolean message(String text, @Nullable Throwable exception, Severity severity, @Nullable ResourceKey key, @Nullable Region region);
}
