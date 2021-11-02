package mb.spoofax.lwb.compiler.statix;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * Compilation exception for Statix in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxStatixCompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R configureFail(SpoofaxStatixConfigureException spoofaxStatixConfigureException);

        R checkFail(KeyedMessages messages);

        R compileFail(Exception cause);
    }

    public static SpoofaxStatixCompileException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxStatixCompileExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxStatixCompileException configureFail(SpoofaxStatixConfigureException spoofaxStatixConfigureException) {
        return withCause(SpoofaxStatixCompileExceptions.configureFail(spoofaxStatixConfigureException), spoofaxStatixConfigureException);
    }

    public static SpoofaxStatixCompileException checkFail(KeyedMessages messages) {
        return SpoofaxStatixCompileExceptions.checkFail(messages);
    }

    public static SpoofaxStatixCompileException compileFail(Exception cause) {
        return withCause(SpoofaxStatixCompileExceptions.compileFail(cause), cause);
    }

    private static SpoofaxStatixCompileException withCause(SpoofaxStatixCompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public SpoofaxStatixCompileExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxStatixCompileExceptions.cases();
    }

    public SpoofaxStatixCompileExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxStatixCompileExceptions.caseOf(this);
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
        return SpoofaxStatixCompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
