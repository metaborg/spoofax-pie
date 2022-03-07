package mb.spoofax.lwb.compiler.dynamix;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * Compilation exception for Dynamix in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxDynamixCompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R configureFail(SpoofaxDynamixConfigureException spoofaxDynamixConfigureException);

        R checkFail(KeyedMessages messages);

        R compileFail(Exception cause);
    }

    public static SpoofaxDynamixCompileException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxDynamixCompileExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxDynamixCompileException configureFail(SpoofaxDynamixConfigureException spoofaxDynamixConfigureException) {
        return withCause(SpoofaxDynamixCompileExceptions.configureFail(spoofaxDynamixConfigureException), spoofaxDynamixConfigureException);
    }

    public static SpoofaxDynamixCompileException checkFail(KeyedMessages messages) {
        return SpoofaxDynamixCompileExceptions.checkFail(messages);
    }

    public static SpoofaxDynamixCompileException compileFail(Exception cause) {
        return withCause(SpoofaxDynamixCompileExceptions.compileFail(cause), cause);
    }

    private static SpoofaxDynamixCompileException withCause(SpoofaxDynamixCompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public SpoofaxDynamixCompileExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxDynamixCompileExceptions.cases();
    }

    public SpoofaxDynamixCompileExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxDynamixCompileExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .configureFail((cause) -> "Configuring Dynamix failed")
            .checkFail((messages) -> "Parsing or checking Dynamix source files failed")
            .compileFail((cause) -> "Dynamix compiler failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return SpoofaxDynamixCompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
