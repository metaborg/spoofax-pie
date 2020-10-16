package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class ConstraintAnalyzerCompilerException extends Exception {
    public interface Cases<R> {
        R mainFileFail(ResourceKey mainFile);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R rootDirectoryFail(ResourcePath rootDirectory);

        R checkFail(KeyedMessages messages);

        R compilerFail(Exception cause);
    }

    public static ConstraintAnalyzerCompilerException mainFileFail(ResourceKey mainFile) {
        return ConstraintAnalyzerCompilerExceptions.mainFileFail(mainFile);
    }

    public static ConstraintAnalyzerCompilerException includeDirectoryFail(ResourcePath includeDirectory) {
        return ConstraintAnalyzerCompilerExceptions.includeDirectoryFail(includeDirectory);
    }

    public static ConstraintAnalyzerCompilerException rootDirectoryFail(ResourcePath rootDirectory) {
        return ConstraintAnalyzerCompilerExceptions.rootDirectoryFail(rootDirectory);
    }

    public static ConstraintAnalyzerCompilerException checkFail(KeyedMessages messages) {
        return ConstraintAnalyzerCompilerExceptions.checkFail(messages);
    }

    public static ConstraintAnalyzerCompilerException compilerFail(Exception cause) {
        return withCause(ConstraintAnalyzerCompilerExceptions.compilerFail(cause), cause);
    }

    private static ConstraintAnalyzerCompilerException withCause(ConstraintAnalyzerCompilerException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public ConstraintAnalyzerCompilerExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
        return ConstraintAnalyzerCompilerExceptions.caseOf(this);
    }

    public Optional<KeyedMessages> getMessages() {
        return ConstraintAnalyzerCompilerExceptions.getMessages(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .mainFileFail((mainFile) -> "Statix main file '" + mainFile + "' does not exist or is not a file")
            .includeDirectoryFail((includeDirectory) -> "Statix include directory '" + includeDirectory + "' does not exist or is not a directory")
            .rootDirectoryFail((rootDirectory) -> "Statix root directory '" + rootDirectory + "' does not exist or is not a directory")
            .checkFail((messages) -> "Parsing or checking Statix source files failed")
            .compilerFail((cause) -> "Statix compiler failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
