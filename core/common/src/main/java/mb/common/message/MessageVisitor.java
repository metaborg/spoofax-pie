package mb.common.message;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MessageVisitor {
    boolean regionOrigin(String text, @Nullable Throwable exception, Severity severity, Region region);

    boolean noOrigin(String text, @Nullable Throwable exception, Severity severity);
}
