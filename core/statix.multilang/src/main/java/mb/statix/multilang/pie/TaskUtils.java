package mb.statix.multilang.pie;

import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.LoggerDebugContext;
import mb.statix.solver.log.NullDebugContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.log.LoggerUtils;

public final class TaskUtils {
    private TaskUtils() {
    }

    static IDebugContext createDebugContext(@Nullable Level logLevel) {
        return logLevel != null ?
            // Statix solver still uses org.metaborg.util.log
            new LoggerDebugContext(LoggerUtils.logger("MLA"), logLevel) : new NullDebugContext();
    }
}
