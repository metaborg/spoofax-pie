package mb.spoofax.lwb.compiler.sdf3;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class Sdf3CompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R mainFileFail(ResourceKey mainFile);

        R sourceDirectoryFail(ResourcePath rootDirectory);

        R checkFail(KeyedMessages messages);

        R parseTableCompileFail(Exception cause);

        R signatureGenerateFail(Exception cause);

        R prettyPrinterGenerateFail(Exception cause);

        R parenthesizerGenerateFail(Exception cause);

        R completionRuntimeGenerateFail(Exception cause);
    }

    public static Sdf3CompileException mainFileFail(ResourceKey mainFile) {
        return Sdf3CompileExceptions.mainFileFail(mainFile);
    }

    public static Sdf3CompileException sourceDirectoryFail(ResourcePath rootDirectory) {
        return Sdf3CompileExceptions.sourceDirectoryFail(rootDirectory);
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

    public Sdf3CompileExceptions.CasesMatchers.TotalMatcher_MainFileFail cases() {
        return Sdf3CompileExceptions.cases();
    }

    public Sdf3CompileExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
        return Sdf3CompileExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .mainFileFail((mainFile) -> "SDF3 main file '" + mainFile + "' does not exist or is not a file")
            .sourceDirectoryFail((sourceDirectory) -> "SDF3 source directory '" + sourceDirectory + "' does not exist or is not a directory")
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
