package mb.dynamix_runtime;

import mb.common.util.ADT;
import mb.stratego.common.StrategoException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

/**
 * Class that represents exceptions that can occur during the execution of a Dynamix
 * specification using the {@link mb.dynamix_runtime.task.DynamixRuntimeRunSpecification} task.
 */
@ADT
public abstract class DynamixSpecificationRunningException extends Exception {
    public interface Cases<R> {
        R analyzeFileFail(Exception ex);

        R getCompiledSpecificationFail(Exception exception);

        R dynamixExecutionFail(StrategoException strategoException);
    }

    public static DynamixSpecificationRunningException analyzeFileFail(Exception exception) {
        return withCause(DynamixSpecificationRunningExceptions.analyzeFileFail(exception), exception);
    }

    public static DynamixSpecificationRunningException getCompiledSpecificationFail(Exception exception) {
        return withCause(DynamixSpecificationRunningExceptions.getCompiledSpecificationFail(exception), exception);
    }

    public static DynamixSpecificationRunningException dynamixExecutionFail(StrategoException strategoException) {
        return withCause(DynamixSpecificationRunningExceptions.dynamixExecutionFail(strategoException), strategoException);
    }

    private static DynamixSpecificationRunningException withCause(DynamixSpecificationRunningException e, Exception cause) {
        e.initCause(cause);
        return e;
    }

    public abstract <R> R match(Cases<R> cases);

    public DynamixSpecificationRunningExceptions.CasesMatchers.TotalMatcher_AnalyzeFileFail cases() {
        return DynamixSpecificationRunningExceptions.cases();
    }

    public DynamixSpecificationRunningExceptions.CaseOfMatchers.TotalMatcher_AnalyzeFileFail caseOf() {
        return DynamixSpecificationRunningExceptions.caseOf(this);
    }

    @Override public String getMessage() {
        return caseOf()
            .analyzeFileFail_("Failed to parse or analyze the input file")
            .getCompiledSpecificationFail_("Loading of compiled Dynamix specification failed")
            .dynamixExecutionFail_("Execution of Dynamix interpreter failed, see console for more details")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
