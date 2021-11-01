package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * Compile exception for SDF3 in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxSdf3CompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R configureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException);

        R checkFail(KeyedMessages messages);

        R parseTableCompileFail(Exception cause);
    }

    public static SpoofaxSdf3CompileException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxSdf3CompileExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxSdf3CompileException configureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException) {
        return withCause(SpoofaxSdf3CompileExceptions.configureFail(spoofaxSdf3ConfigureException), spoofaxSdf3ConfigureException);
    }

    public static SpoofaxSdf3CompileException checkFail(KeyedMessages messages) {
        return SpoofaxSdf3CompileExceptions.checkFail(messages);
    }

    public static SpoofaxSdf3CompileException parseTableCompileFail(Exception cause) {
        return withCause(SpoofaxSdf3CompileExceptions.parseTableCompileFail(cause), cause);
    }

    private static SpoofaxSdf3CompileException withCause(SpoofaxSdf3CompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public SpoofaxSdf3CompileExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxSdf3CompileExceptions.cases();
    }

    public SpoofaxSdf3CompileExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxSdf3CompileExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .configureFail((cause) -> "Configuring SDF3 failed")
            .checkFail((messages) -> "Parsing or checking SDF3 source files failed; see error messages")
            .parseTableCompileFail((cause) -> "Compile parse table from SDF3 failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return SpoofaxSdf3CompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
