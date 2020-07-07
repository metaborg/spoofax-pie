package mb.constraint.common;

import mb.common.util.ADT;
import mb.stratego.common.StrategoException;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class ConstraintAnalyzerException extends Exception {
    public interface Cases<R> {
        R strategoInvokeFail(StrategoException cause);
    }

    public static ConstraintAnalyzerException strategoInvokeFail(StrategoException cause) {
        final ConstraintAnalyzerException e = ConstraintAnalyzerExceptions.strategoInvokeFail(cause);
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(ConstraintAnalyzerException.Cases<R> cases);

    @Override public String getMessage() {
        return ConstraintAnalyzerExceptions.cases()
            .strategoInvokeFail(StrategoException::getMessage)
            .apply(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();


    protected ConstraintAnalyzerException() {
        super(null, null, true, false);
    }
}
