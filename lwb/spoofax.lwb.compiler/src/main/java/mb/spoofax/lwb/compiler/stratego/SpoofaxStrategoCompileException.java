package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.config.StrategoCompileConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * Compile exception for Stratego in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxStrategoCompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R configureFail(SpoofaxStrategoConfigureException spoofaxStrategoConfigureException);

        R checkFail(KeyedMessages messages, StrategoAnalyzeConfig checkConfig);

        R compileFail(Exception cause, StrategoCompileConfig compileConfig);
    }

    public static SpoofaxStrategoCompileException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxStrategoCompileExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxStrategoCompileException configureFail(SpoofaxStrategoConfigureException spoofaxStrategoConfigureException) {
        return withCause(SpoofaxStrategoCompileExceptions.configureFail(spoofaxStrategoConfigureException), spoofaxStrategoConfigureException);
    }

    public static SpoofaxStrategoCompileException checkFail(KeyedMessages messages, StrategoAnalyzeConfig checkConfig) {
        return SpoofaxStrategoCompileExceptions.checkFail(messages, checkConfig);
    }

    public static SpoofaxStrategoCompileException compileFail(Exception cause, StrategoCompileConfig compileConfig) {
        return withCause(SpoofaxStrategoCompileExceptions.compileFail(cause, compileConfig), cause);
    }

    private static SpoofaxStrategoCompileException withCause(SpoofaxStrategoCompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public SpoofaxStrategoCompileExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxStrategoCompileExceptions.cases();
    }

    public SpoofaxStrategoCompileExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxStrategoCompileExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .configureFail((cause) -> "Configuring Stratego failed")
            .checkFail((messages, checkConfig) -> "Checking Stratego source files with config " + checkConfig + " failed")
            .compileFail((cause, compileConfig) -> "Compiling Stratego with config " + compileConfig + " failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return SpoofaxStrategoCompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
