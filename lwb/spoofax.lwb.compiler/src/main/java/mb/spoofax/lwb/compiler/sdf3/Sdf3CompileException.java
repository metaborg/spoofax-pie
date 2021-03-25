package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class Sdf3CompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R configureFail(Sdf3ConfigureException sdf3ConfigureException);

        R checkFail(KeyedMessages messages);

        R parseTableCompileFail(Exception cause);

        R signatureGenerateFail(Exception cause);

        R prettyPrinterGenerateFail(Exception cause);

        R parenthesizerGenerateFail(Exception cause);

        R completionRuntimeGenerateFail(Exception cause);
    }

    public static Sdf3CompileException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(Sdf3CompileExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static Sdf3CompileException configureFail(Sdf3ConfigureException sdf3ConfigureException) {
        return withCause(Sdf3CompileExceptions.configureFail(sdf3ConfigureException), sdf3ConfigureException);
    }

    public static Sdf3CompileException checkFail(KeyedMessages messages) {
        return Sdf3CompileExceptions.checkFail(messages);
    }

    public static Sdf3CompileException parseTableCompileFail(Exception cause) {
        return withCause(Sdf3CompileExceptions.parseTableCompileFail(cause), cause);
    }

    public static Sdf3CompileException signatureGenerateFail(Exception cause) {
        return withCause(Sdf3CompileExceptions.signatureGenerateFail(cause), cause);
    }

    public static Sdf3CompileException prettyPrinterGenerateFail(Exception cause) {
        return withCause(Sdf3CompileExceptions.prettyPrinterGenerateFail(cause), cause);
    }

    public static Sdf3CompileException parenthesizerGenerateFail(Exception cause) {
        return withCause(Sdf3CompileExceptions.parenthesizerGenerateFail(cause), cause);
    }

    public static Sdf3CompileException completionRuntimeGenerateFail(Exception cause) {
        return withCause(Sdf3CompileExceptions.completionRuntimeGenerateFail(cause), cause);
    }

    private static Sdf3CompileException withCause(Sdf3CompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public Sdf3CompileExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return Sdf3CompileExceptions.cases();
    }

    public Sdf3CompileExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return Sdf3CompileExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .configureFail((cause) -> "Configuring SDF3 failed")
            .checkFail((messages) -> "Parsing or checking SDF3 source files failed; see error messages")
            .parseTableCompileFail((cause) -> "Compile parse table from SDF3 failed unexpectedly")
            .signatureGenerateFail((cause) -> "Generate stratego signature from SDF3 failed unexpectedly")
            .prettyPrinterGenerateFail((cause) -> "Generate pretty-printer from SDF3 failed unexpectedly")
            .parenthesizerGenerateFail((cause) -> "Generate parenthesizer from SDF3 failed unexpectedly")
            .completionRuntimeGenerateFail((cause) -> "Generate completion runtime from SDF3 failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return Sdf3CompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
