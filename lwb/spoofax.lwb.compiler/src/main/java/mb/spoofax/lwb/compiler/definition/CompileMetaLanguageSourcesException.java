package mb.spoofax.lwb.compiler.definition;

import mb.common.util.ADT;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCompileException;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3CompileException;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixCompileException;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoCompileException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class CompileMetaLanguageSourcesException extends Exception {
    public interface Cases<R> {
        R sdf3CompileFail(SpoofaxSdf3CompileException spoofaxSdf3CompileException);

        R esvCompileFail(SpoofaxEsvCompileException spoofaxEsvCompileException);

        R statixCompileFail(SpoofaxStatixCompileException spoofaxStatixCompileException);

        R strategoCompileFail(SpoofaxStrategoCompileException spoofaxStrategoCompileException);
    }

    public static CompileMetaLanguageSourcesException sdf3CompileFail(SpoofaxSdf3CompileException cause) {
        return withCause(CompileMetaLanguageSourcesException.sdf3CompileFail(cause), cause);
    }

    public static CompileMetaLanguageSourcesException esvCompileFail(SpoofaxEsvCompileException cause) {
        return withCause(CompileMetaLanguageSourcesException.esvCompileFail(cause), cause);
    }

    public static CompileMetaLanguageSourcesException statixCompileFail(SpoofaxStatixCompileException cause) {
        return withCause(CompileMetaLanguageSourcesException.statixCompileFail(cause), cause);
    }

    public static CompileMetaLanguageSourcesException strategoCompileFail(SpoofaxStrategoCompileException cause) {
        return withCause(CompileMetaLanguageSourcesException.strategoCompileFail(cause), cause);
    }

    private static CompileMetaLanguageSourcesException withCause(CompileMetaLanguageSourcesException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CompileMetaLanguageSourcesExceptions.CasesMatchers.TotalMatcher_Sdf3CompileFail cases() {
        return CompileMetaLanguageSourcesExceptions.cases();
    }

    public CompileMetaLanguageSourcesExceptions.CaseOfMatchers.TotalMatcher_Sdf3CompileFail caseOf() {
        return CompileMetaLanguageSourcesExceptions.caseOf(this);
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
