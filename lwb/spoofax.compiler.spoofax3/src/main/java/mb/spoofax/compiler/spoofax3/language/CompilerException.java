package mb.spoofax.compiler.spoofax3.language;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class CompilerException extends Exception {
    public interface Cases<R> {
        R parserCompilerFail(ParserCompilerException parserCompilerException);

        R stylerCompilerFail(StylerCompilerException stylerCompilerException);

        R strategoRuntimeCompilerFail(StrategoCompilerException strategoCompilerException);
    }

    public static CompilerException parserCompilerFail(ParserCompilerException cause) {
        return withCause(CompilerExceptions.parserCompilerFail(cause), cause);
    }

    public static CompilerException stylerCompilerFail(StylerCompilerException cause) {
        return withCause(CompilerExceptions.stylerCompilerFail(cause), cause);
    }

    public static CompilerException strategoRuntimeCompilerFail(StrategoCompilerException cause) {
        return withCause(CompilerExceptions.strategoRuntimeCompilerFail(cause), cause);
    }

    private static CompilerException withCause(CompilerException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CompilerExceptions.CasesMatchers.TotalMatcher_ParserCompilerFail cases() {
        return CompilerExceptions.cases();
    }

    public CompilerExceptions.CaseOfMatchers.TotalMatcher_ParserCompilerFail caseOf() {
        return CompilerExceptions.caseOf(this);
    }


    @Override public @NonNull String getMessage() {
        return caseOf()
            .parserCompilerFail((cause) -> "Parser compiler failed")
            .stylerCompilerFail((cause) -> "Styler compiler failed")
            .strategoRuntimeCompilerFail((cause) -> "Stratego compiler failed")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
