package mb.statix.multilang.pie;

import mb.common.result.ExceptionalSupplier;
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

public final class TaskUtils {
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


    public static <O> Result<O, MultiLangAnalysisException> executeIOWrapped(
        IOExceptionThrowingFunction<Result<O, MultiLangAnalysisException>> function,
        String exceptionMessage
    ) {
        try {
            return function.apply();
        } catch(IOException e) {
            return Result.ofErr(new MultiLangAnalysisException(exceptionMessage, e));
        }
    }

    // TODO: Make interface more generic
    public static <O> Result<O, MultiLangAnalysisException> executeWrapped(
        ExceptionalSupplier<Result<O, MultiLangAnalysisException>, Exception> function,
        String exceptionMessage
    ) {
        try {
            return function.get();
        } catch(Exception e) {
            return Result.ofErr(new MultiLangAnalysisException(exceptionMessage, e));
        }
    }

    public static <O> Result<O, MultiLangAnalysisException> executeInterruptionWrapped(
        InterruptedExceptionThrowingFunction<Result<O, MultiLangAnalysisException>> function,
        String exceptionMessage
    ) {
        try {
            return function.apply();
        } catch(InterruptedException e) {
            return Result.ofErr(new MultiLangAnalysisException(exceptionMessage, e));
        }
    }

    public interface InterruptedExceptionThrowingFunction<O> {
        O apply() throws InterruptedException;
    }

    public interface IOExceptionThrowingFunction<O> {
        O apply() throws IOException;
    }
}
