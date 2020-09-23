package mb.spoofax.compiler.spoofax3.language;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class CompilerException extends Exception {
    public interface Cases<R> {
        R parserCompilerFail(ParserCompilerException parserCompilerException);

        R strategoRuntimeCompilerFail(Exception exception);
    }

    public static CompilerException parserCompilerFail(ParserCompilerException cause) {
        final CompilerException e = CompilerExceptions.parserCompilerFail(cause);
        e.initCause(cause);
        return e;
    }

    public static CompilerException strategoRuntimeCompilerFail(Exception cause) {
        final CompilerException e = CompilerExceptions.strategoRuntimeCompilerFail(cause);
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public CompilerExceptions.CaseOfMatchers.TotalMatcher_ParserCompilerFail caseOf() {
        return CompilerExceptions.caseOf(this);
    }


    @Override public @NonNull String getMessage() {
        return caseOf()
            .parserCompilerFail((cause) -> "Parser compiler failed")
            .strategoRuntimeCompilerFail((cause) -> "Stratego runtime compiler failed")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
