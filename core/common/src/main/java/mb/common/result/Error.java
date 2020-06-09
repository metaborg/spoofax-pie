package mb.common.result;

import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface Error extends Serializable {
    String getDescription();

    @Nullable Error getCause();

    @Nullable ListView<StackTraceElement> getStackTrace();
}
