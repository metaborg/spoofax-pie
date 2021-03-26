package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.config.StrategoCompileConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class StrategoCompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R configureFail(StrategoConfigureException strategoConfigureException);

        R checkFail(KeyedMessages messages, StrategoAnalyzeConfig checkConfig);

        R compileFail(Exception cause, StrategoCompileConfig compileConfig);
    }

    public static StrategoCompileException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(StrategoCompileExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static StrategoCompileException configureFail(StrategoConfigureException strategoConfigureException) {
        return withCause(StrategoCompileExceptions.configureFail(strategoConfigureException), strategoConfigureException);
    }

    public static StrategoCompileException checkFail(KeyedMessages messages, StrategoAnalyzeConfig checkConfig) {
        return StrategoCompileExceptions.checkFail(messages, checkConfig);
    }

    public static StrategoCompileException compileFail(Exception cause, StrategoCompileConfig compileConfig) {
        return withCause(StrategoCompileExceptions.compileFail(cause, compileConfig), cause);
    }

    private static StrategoCompileException withCause(StrategoCompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public StrategoCompileExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return StrategoCompileExceptions.cases();
    }

    public StrategoCompileExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return StrategoCompileExceptions.caseOf(this);
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
        return StrategoCompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
