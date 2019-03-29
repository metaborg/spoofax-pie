package mb.spoofax.core.language;

import mb.common.style.Styling;
import mb.fs.api.path.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface StylingService {
    @Nullable Styling getStyling(FSPath path);
}
