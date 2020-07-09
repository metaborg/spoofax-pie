package mb.statix.multilang.pie;

import mb.common.result.Result;
import mb.statix.multilang.FileResult;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.LoggerDebugContext;
import mb.statix.solver.log.NullDebugContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.log.LoggerUtils;

import java.io.IOException;

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


    static <O> Result<O, MultiLangAnalysisException> executeIOWrapped(
        IOExceptionThrowingFunction<Result<O, MultiLangAnalysisException>> function,
        String exceptionMessage
    ) {
        try {
            return function.apply();
        } catch(IOException e) {
            return Result.ofErr(new MultiLangAnalysisException(exceptionMessage, e));
        }
    }

    interface IOExceptionThrowingFunction<O> {
        O apply() throws IOException;
    }


    static <O> Result<O, MultiLangAnalysisException> executeInterruptionWrapped(
        InterruptedExceptionThrowingFunction<Result<O, MultiLangAnalysisException>> function,
        String exceptionMessage
    ) {
        try {
            return function.apply();
        } catch(InterruptedException e) {
            return Result.ofErr(new MultiLangAnalysisException(exceptionMessage, e));
        }
    }

    interface InterruptedExceptionThrowingFunction<O> {
        O apply() throws InterruptedException;
    }
}
