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
public abstract class EsvCompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R configureFail(EsvConfigureException esvConfigureException);

        R checkFail(KeyedMessages messages);

        R compileFail(Exception cause);
    }

    public static EsvCompileException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(EsvCompileExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static EsvCompileException configureFail(EsvConfigureException esvConfigureException) {
        return withCause(EsvCompileExceptions.configureFail(esvConfigureException), esvConfigureException);
    }

    public static EsvCompileException checkFail(KeyedMessages messages) {
        return EsvCompileExceptions.checkFail(messages);
    }

    public static EsvCompileException compileFail(Exception cause) {
        return withCause(EsvCompileExceptions.compileFail(cause), cause);
    }

    private static EsvCompileException withCause(EsvCompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static EsvCompileExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return EsvCompileExceptions.cases();
    }

    public EsvCompileExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return EsvCompileExceptions.caseOf(this);
    }

    public Optional<KeyedMessages> getMessages() {
        return EsvCompileExceptions.getMessages(this);
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
        return EsvCompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
