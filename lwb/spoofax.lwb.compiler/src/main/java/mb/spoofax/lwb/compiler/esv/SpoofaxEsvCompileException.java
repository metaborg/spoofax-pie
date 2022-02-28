package mb.spoofax.lwb.compiler.esv;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * Error for ESV compile task in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxEsvCompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R configureFail(SpoofaxEsvConfigureException esvConfigureException);

        R checkFail(KeyedMessages messages);

        R compileFail(Exception cause);
    }

    public static SpoofaxEsvCompileException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxEsvCompileExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxEsvCompileException configureFail(SpoofaxEsvConfigureException esvConfigureException) {
        return withCause(SpoofaxEsvCompileExceptions.configureFail(esvConfigureException), esvConfigureException);
    }

    public static SpoofaxEsvCompileException checkFail(KeyedMessages messages) {
        return SpoofaxEsvCompileExceptions.checkFail(messages);
    }

    public static SpoofaxEsvCompileException compileFail(Exception cause) {
        return withCause(SpoofaxEsvCompileExceptions.compileFail(cause), cause);
    }

    private static SpoofaxEsvCompileException withCause(SpoofaxEsvCompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static SpoofaxEsvCompileExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxEsvCompileExceptions.cases();
    }

    public SpoofaxEsvCompileExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxEsvCompileExceptions.caseOf(this);
    }

    public Optional<KeyedMessages> getMessages() {
        return SpoofaxEsvCompileExceptions.getMessages(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .configureFail((cause) -> "Configuring ESV failed")
            .checkFail((messages) -> "Parsing or checking ESV source files failed")
            .compileFail((cause) -> "ESV compiler failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return SpoofaxEsvCompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
