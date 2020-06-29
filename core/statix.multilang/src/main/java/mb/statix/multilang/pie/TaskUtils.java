package mb.statix.multilang.pie;

import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.LoggerDebugContext;
import mb.statix.solver.log.NullDebugContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.log.LoggerUtils;

final class TaskUtils {
    private TaskUtils() {
    }

    static IDebugContext createDebugContext(String name, @Nullable Level logLevel) {
        return logLevel != null ?
            // Statix solver still uses org.metaborg.util.log
            new LoggerDebugContext(LoggerUtils.logger(name), logLevel) : new NullDebugContext();
    }

    static IDebugContext createDebugContext(Class<?> cls, @Nullable Level logLevel) {
        return logLevel != null ?
            // Statix solver still uses org.metaborg.util.log
            new LoggerDebugContext(LoggerUtils.logger(cls), logLevel) : new NullDebugContext();
    }
}
