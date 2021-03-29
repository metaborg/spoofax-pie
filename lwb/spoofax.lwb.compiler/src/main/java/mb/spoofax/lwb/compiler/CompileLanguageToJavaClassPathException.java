package mb.spoofax.lwb.compiler;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Optional;

@ADT
public abstract class CompileLanguageToJavaClassPathException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R walkJavaSourceFilesFail(IOException ioException);

        R compileLanguageFail(CompileLanguage.CompileException compileException);

        R javaCompilationFail(KeyedMessages messages);
    }

    public static CompileLanguageToJavaClassPathException getConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(CompileLanguageToJavaClassPathExceptions.getConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static CompileLanguageToJavaClassPathException walkJavaSourceFilesFail(IOException ioException) {
        return withCause(CompileLanguageToJavaClassPathExceptions.walkJavaSourceFilesFail(ioException), ioException);
    }

    public static CompileLanguageToJavaClassPathException compileLanguageFail(CompileLanguage.CompileException compileException) {
        return withCause(CompileLanguageToJavaClassPathExceptions.compileLanguageFail(compileException), compileException);
    }

    public static CompileLanguageToJavaClassPathException javaCompilationFail(KeyedMessages messages) {
        return CompileLanguageToJavaClassPathExceptions.javaCompilationFail(messages);
    }


    private static CompileLanguageToJavaClassPathException withCause(CompileLanguageToJavaClassPathException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CompileLanguageToJavaClassPathExceptions.CasesMatchers.TotalMatcher_GetConfigurationFail cases() {
        return CompileLanguageToJavaClassPathExceptions.cases();
    }

    public CompileLanguageToJavaClassPathExceptions.CaseOfMatchers.TotalMatcher_GetConfigurationFail caseOf() {
        return CompileLanguageToJavaClassPathExceptions.caseOf(this);
    }


    @Override public @NonNull String getMessage() {
        return cases()
            .getConfigurationFail((e) -> "Failed to get configuration")
            .walkJavaSourceFilesFail((e) -> "Walking Java source files failed")
            .compileLanguageFail((e) -> "Compiling language failed")
            .javaCompilationFail((e) -> "Java compilation failed")
            .apply(this);
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return CompileLanguageToJavaClassPathExceptions.getMessages(this);
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
