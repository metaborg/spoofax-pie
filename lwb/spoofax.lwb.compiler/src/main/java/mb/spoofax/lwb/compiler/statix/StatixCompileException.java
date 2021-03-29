package mb.spoofax.lwb.compiler.statix;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class StatixCompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R configureFail(StatixConfigureException statixConfigureException);

        R checkFail(KeyedMessages messages);

        R compileFail(Exception cause);
    }

    public static StatixCompileException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(StatixCompileExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static StatixCompileException configureFail(StatixConfigureException statixConfigureException) {
        return withCause(StatixCompileExceptions.configureFail(statixConfigureException), statixConfigureException);
    }

    public static StatixCompileException checkFail(KeyedMessages messages) {
        return StatixCompileExceptions.checkFail(messages);
    }

    public static StatixCompileException compileFail(Exception cause) {
        return withCause(StatixCompileExceptions.compileFail(cause), cause);
    }

    private static StatixCompileException withCause(StatixCompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public StatixCompileExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return StatixCompileExceptions.cases();
    }

    public StatixCompileExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return StatixCompileExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .configureFail((cause) -> "Configuring Statix failed")
            .checkFail((messages) -> "Parsing or checking Statix source files failed")
            .compileFail((cause) -> "Statix compiler failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return StatixCompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
