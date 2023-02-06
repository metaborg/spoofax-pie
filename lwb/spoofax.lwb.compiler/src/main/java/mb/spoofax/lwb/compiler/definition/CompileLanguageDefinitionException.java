package mb.spoofax.lwb.compiler.definition;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class CompileLanguageDefinitionException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R compileMetaLanguageSourcesFail(CompileMetaLanguageSourcesException compileMetaLanguageSourcesException);

        R javaCompilationFail(KeyedMessages messages);
    }

    public static CompileLanguageDefinitionException getConfigurationFail(CfgRootDirectoryToObjectException cause) {
        return withCause(CompileLanguageDefinitionExceptions.getConfigurationFail(cause), cause);
    }

    public static CompileLanguageDefinitionException compileMetaLanguageSourcesFail(CompileMetaLanguageSourcesException cause) {
        return withCause(CompileLanguageDefinitionExceptions.compileMetaLanguageSourcesFail(cause), cause);
    }

    public static CompileLanguageDefinitionException javaCompilationFail(KeyedMessages messages) {
        return CompileLanguageDefinitionExceptions.javaCompilationFail(messages);
    }


    private static CompileLanguageDefinitionException withCause(CompileLanguageDefinitionException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static CompileLanguageDefinitionExceptions.CasesMatchers.TotalMatcher_GetConfigurationFail cases() {
        return CompileLanguageDefinitionExceptions.cases();
    }

    public CompileLanguageDefinitionExceptions.CaseOfMatchers.TotalMatcher_GetConfigurationFail caseOf() {
        return CompileLanguageDefinitionExceptions.caseOf(this);
    }


    @Override public @NonNull String getMessage() {
        return cases()
            .getConfigurationFail((e) -> "Failed to get configuration")
            .compileMetaLanguageSourcesFail((e) -> "Meta-language sources compilation failed")
            .javaCompilationFail((e) -> "Java compilation failed")
            .apply(this);
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return CompileLanguageDefinitionExceptions.getMessages(this);
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
