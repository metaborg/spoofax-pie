package mb.spoofax.lwb.compiler;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class CompileLanguageWithCfgToJavaClassPathException extends Exception {
    public interface Cases<R> {
        R createInputFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R languageCompilerFail(CompileLanguageToJavaClassPathException compileLanguageToJavaClassPathException);
    }

    public static CompileLanguageWithCfgToJavaClassPathException createInputFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(CompileLanguageWithCfgToJavaClassPathExceptions.createInputFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static CompileLanguageWithCfgToJavaClassPathException languageCompilerFail(CompileLanguageToJavaClassPathException compileLanguageToJavaClassPathException) {
        return withCause(CompileLanguageWithCfgToJavaClassPathExceptions.languageCompilerFail(compileLanguageToJavaClassPathException), compileLanguageToJavaClassPathException);
    }


    private static CompileLanguageWithCfgToJavaClassPathException withCause(CompileLanguageWithCfgToJavaClassPathException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CompileLanguageWithCfgToJavaClassPathExceptions.CasesMatchers.TotalMatcher_CreateInputFail cases() {
        return CompileLanguageWithCfgToJavaClassPathExceptions.cases();
    }

    public CompileLanguageWithCfgToJavaClassPathExceptions.CaseOfMatchers.TotalMatcher_CreateInputFail caseOf() {
        return CompileLanguageWithCfgToJavaClassPathExceptions.caseOf(this);
    }


    @Override public @NonNull String getMessage() {
        return cases()
            .createInputFail((e) -> "Creating language compiler input failed")
            .languageCompilerFail((e) -> "Language compiler failed")
            .apply(this);
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
