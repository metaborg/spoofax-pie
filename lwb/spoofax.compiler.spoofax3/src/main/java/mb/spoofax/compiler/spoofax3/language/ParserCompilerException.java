package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class ParserCompilerException extends Exception {
    public interface Cases<R> {
        R mainFileFail(ResourceKey mainFile);

        R rootDirectoryFail(ResourcePath rootDirectory);

        R checkFail(KeyedMessages messages);

        R parseTableCompilerFail(Exception cause);

        R signatureGeneratorFail(Exception cause);

        R prettyPrinterGeneratorFail(Exception cause);

        R parenthesizerGeneratorFail(Exception cause);

        R completionRuntimeGeneratorFail(Exception cause);
    }

    public static ParserCompilerException mainFileFail(ResourceKey mainFile) {
        return ParserCompilerExceptions.mainFileFail(mainFile);
    }

    public static ParserCompilerException rootDirectoryFail(ResourcePath rootDirectory) {
        return ParserCompilerExceptions.rootDirectoryFail(rootDirectory);
    }

    public static ParserCompilerException checkFail(KeyedMessages messages) {
        return ParserCompilerExceptions.checkFail(messages);
    }

    public static ParserCompilerException parseTableCompilerFail(Exception cause) {
        return withCause(ParserCompilerExceptions.parseTableCompilerFail(cause), cause);
    }

    public static ParserCompilerException signatureGeneratorFail(Exception cause) {
        return withCause(ParserCompilerExceptions.signatureGeneratorFail(cause), cause);
    }

    public static ParserCompilerException prettyPrinterGeneratorFail(Exception cause) {
        return withCause(ParserCompilerExceptions.prettyPrinterGeneratorFail(cause), cause);
    }

    public static ParserCompilerException parenthesizerGeneratorFail(Exception cause) {
        return withCause(ParserCompilerExceptions.parenthesizerGeneratorFail(cause), cause);
    }

    public static ParserCompilerException completionRuntimeGeneratorFail(Exception cause) {
        return withCause(ParserCompilerExceptions.completionRuntimeGeneratorFail(cause), cause);
    }

    private static ParserCompilerException withCause(ParserCompilerException e, Exception cause) {
        e.initCause(cause);
        return e;
    }



    public abstract <R> R match(Cases<R> cases);

    public ParserCompilerExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
        return ParserCompilerExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .mainFileFail((mainFile) -> "Main file '" + mainFile + "' does not exist or is not a file")
            .rootDirectoryFail((rootDirectory) -> "Root directory '" + rootDirectory + "' does not exist or is not a directory")
            .checkFail((messages) -> "Parsing or checking SDF3 source files failed; see error messages")
            .parseTableCompilerFail((cause) -> "Parse table compiler failed unexpectedly")
            .signatureGeneratorFail((cause) -> "Stratego signature generator failed unexpectedly")
            .prettyPrinterGeneratorFail((cause) -> "Pretty-printer generator failed unexpectedly")
            .parenthesizerGeneratorFail((cause) -> "Parenthesizer generator failed unexpectedly")
            .completionRuntimeGeneratorFail((cause) -> "Completion runtime generator failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
