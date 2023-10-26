package mb.tim_runtime;

import mb.common.util.ADT;
import mb.stratego.common.StrategoException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.TermType;

/**
 * Class that represents exceptions that can occur during the execution of a Tim program.
 */
@ADT
public abstract class TimExecutionException extends Exception {
    public interface Cases<R> {
        R timExecutionFail(StrategoException strategoException);

        R timExecutionResultNotString(TermType type);
    }

    public static TimExecutionException timExecutionFail(StrategoException strategoException) {
        return withCause(TimExecutionExceptions.timExecutionFail(strategoException), strategoException);
    }

    public static TimExecutionException timExecutionResultNotString(TermType type) {
        return TimExecutionExceptions.timExecutionResultNotString(type);
    }

    private static TimExecutionException withCause(TimExecutionException e, Exception cause) {
        e.initCause(cause);
        return e;
    }

    public abstract <R> R match(Cases<R> cases);

    public TimExecutionExceptions.CasesMatchers.TotalMatcher_TimExecutionFail cases() {
        return TimExecutionExceptions.cases();
    }

    public TimExecutionExceptions.CaseOfMatchers.TotalMatcher_TimExecutionFail caseOf() {
        return TimExecutionExceptions.caseOf(this);
    }

    @Override public String getMessage() {
        return caseOf()
            .timExecutionFail_("Failed to execute Tim program. See console for more details.")
            .timExecutionResultNotString_("Result of Tim execution was not a string.")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
