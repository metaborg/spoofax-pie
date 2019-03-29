package mb.spoofax.core.language;

import mb.common.message.MessageCollection;
import mb.fs.api.path.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MessageService {
    @Nullable MessageCollection getMessages(FSPath path);
}
