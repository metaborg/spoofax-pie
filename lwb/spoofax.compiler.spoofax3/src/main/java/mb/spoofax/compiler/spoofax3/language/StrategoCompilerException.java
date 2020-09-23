package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class StrategoCompilerException extends Exception {
    public interface Cases<R> {
        R mainFileFail(ResourceKey mainFile);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R rootDirectoryFail(ResourcePath rootDirectory);

        R checkFail(KeyedMessages messages);

        R compilerFail(Exception cause);
    }

    public static StrategoCompilerException mainFileFail(ResourceKey mainFile) {
        return StrategoCompilerExceptions.mainFileFail(mainFile);
    }

    public static StrategoCompilerException includeDirectoryFail(ResourcePath includeDirectory) {
        return StrategoCompilerExceptions.includeDirectoryFail(includeDirectory);
    }

    public static StrategoCompilerException rootDirectoryFail(ResourcePath rootDirectory) {
        return StrategoCompilerExceptions.rootDirectoryFail(rootDirectory);
    }

    public static StrategoCompilerException checkFail(KeyedMessages messages) {
        return StrategoCompilerExceptions.checkFail(messages);
    }

    public static StrategoCompilerException compilerFail(Exception cause) {
        final StrategoCompilerException e = StrategoCompilerExceptions.compilerFail(cause);
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public StrategoCompilerExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
        return StrategoCompilerExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .mainFileFail((mainFile) -> "Main file '" + mainFile + "' does not exist or is not a file")
            .includeDirectoryFail((includeDirectory) -> "Include directory '" + includeDirectory + "' does not exist or is not a directory")
            .rootDirectoryFail((rootDirectory) -> "Root directory '" + rootDirectory + "' does not exist or is not a directory")
            .checkFail((messages) -> "Parsing or checking Stratego source files failed; see error messages")
            .compilerFail((cause) -> "Stratego compiler failed unexpectedly; see cause")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
