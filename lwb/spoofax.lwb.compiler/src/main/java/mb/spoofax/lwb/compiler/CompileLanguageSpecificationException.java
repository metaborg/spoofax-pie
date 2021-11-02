package mb.spoofax.lwb.compiler;

import mb.common.util.ADT;
import mb.spoofax.lwb.compiler.esv.EsvCompileException;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3CompileException;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixCompileException;
import mb.spoofax.lwb.compiler.stratego.StrategoCompileException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class CompileLanguageSpecificationException extends Exception {
    public interface Cases<R> {
        R sdf3CompileFail(SpoofaxSdf3CompileException spoofaxSdf3CompileException);

        R esvCompileFail(EsvCompileException esvCompileException);

        R statixCompileFail(SpoofaxStatixCompileException spoofaxStatixCompileException);

        R strategoCompileFail(StrategoCompileException strategoCompileException);
    }

    public static CompileLanguageSpecificationException sdf3CompileFail(SpoofaxSdf3CompileException cause) {
        return withCause(CompileLanguageSpecificationExceptions.sdf3CompileFail(cause), cause);
    }

    public static CompileLanguageSpecificationException esvCompileFail(EsvCompileException cause) {
        return withCause(CompileLanguageSpecificationExceptions.esvCompileFail(cause), cause);
    }

    public static CompileLanguageSpecificationException statixCompileFail(SpoofaxStatixCompileException cause) {
        return withCause(CompileLanguageSpecificationExceptions.statixCompileFail(cause), cause);
    }

    public static CompileLanguageSpecificationException strategoCompileFail(StrategoCompileException cause) {
        return withCause(CompileLanguageSpecificationExceptions.strategoCompileFail(cause), cause);
    }

    private static CompileLanguageSpecificationException withCause(CompileLanguageSpecificationException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CompileLanguageSpecificationExceptions.CasesMatchers.TotalMatcher_Sdf3CompileFail cases() {
        return CompileLanguageSpecificationExceptions.cases();
    }

    public CompileLanguageSpecificationExceptions.CaseOfMatchers.TotalMatcher_Sdf3CompileFail caseOf() {
        return CompileLanguageSpecificationExceptions.caseOf(this);
    }


    @Override public @NonNull String getMessage() {
        return cases()
            .sdf3CompileFail((cause) -> "SDF3 compiler failed")
            .esvCompileFail((cause) -> "ESV compiler failed")
            .statixCompileFail((cause) -> "Statix compiler failed")
            .strategoCompileFail((cause) -> "Stratego compiler failed")
            .apply(this);
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
