package mb.statix.multilang.pie;

import mb.common.result.ExceptionalSupplier;
import mb.common.result.Result;
import mb.statix.multilang.MultiLangAnalysisException;
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

    public static <O> Result<O, MultiLangAnalysisException> executeWrapped(
        ExceptionalSupplier<Result<O, MultiLangAnalysisException>, ? extends Exception> function,
        String exceptionMessage
    ) {
        try {
            return function.get();
        } catch(Exception e) {
            return Result.ofErr(MultiLangAnalysisException.wrapIfNeeded(exceptionMessage, e));
        }
    }

    public static <O> Result<O, MultiLangAnalysisException> executeWrapped(
        ExceptionalSupplier<Result<O, MultiLangAnalysisException>, ? extends Exception> function
    ) {
        try {
            return function.get();
        } catch(Exception e) {
            return Result.ofErr(MultiLangAnalysisException.wrapIfNeeded(e));
        }
    }
}
