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
public abstract class CompileLanguageException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R walkJavaSourceFilesFail(IOException ioException);

        R compileLanguageFail(CompileLanguageSpecificationException compileLanguageSpecificationException);

        R javaCompilationFail(KeyedMessages messages);
    }

    public static CompileLanguageException getConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(CompileLanguageExceptions.getConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static CompileLanguageException walkJavaSourceFilesFail(IOException ioException) {
        return withCause(CompileLanguageExceptions.walkJavaSourceFilesFail(ioException), ioException);
    }

    public static CompileLanguageException compileLanguageFail(CompileLanguageSpecificationException compileLanguageSpecificationException) {
        return withCause(CompileLanguageExceptions.compileLanguageFail(compileLanguageSpecificationException), compileLanguageSpecificationException);
    }

    public static CompileLanguageException javaCompilationFail(KeyedMessages messages) {
        return CompileLanguageExceptions.javaCompilationFail(messages);
    }


    private static CompileLanguageException withCause(CompileLanguageException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CompileLanguageExceptions.CasesMatchers.TotalMatcher_GetConfigurationFail cases() {
        return CompileLanguageExceptions.cases();
    }

    public CompileLanguageExceptions.CaseOfMatchers.TotalMatcher_GetConfigurationFail caseOf() {
        return CompileLanguageExceptions.caseOf(this);
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
        return CompileLanguageExceptions.getMessages(this);
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
