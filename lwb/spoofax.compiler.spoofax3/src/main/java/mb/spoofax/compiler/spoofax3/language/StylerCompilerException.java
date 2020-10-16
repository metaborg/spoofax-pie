package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class StylerCompilerException extends Exception {
    public interface Cases<R> {
        R mainFileFail(ResourceKey mainFile);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R rootDirectoryFail(ResourcePath rootDirectory);

        R checkFail(KeyedMessages messages);

        R compilerFail(Exception cause);
    }

    public static StylerCompilerException mainFileFail(ResourceKey mainFile) {
        return StylerCompilerExceptions.mainFileFail(mainFile);
    }

    public static StylerCompilerException includeDirectoryFail(ResourcePath includeDirectory) {
        return StylerCompilerExceptions.includeDirectoryFail(includeDirectory);
    }

    public static StylerCompilerException rootDirectoryFail(ResourcePath rootDirectory) {
        return StylerCompilerExceptions.rootDirectoryFail(rootDirectory);
    }

    public static StylerCompilerException checkFail(KeyedMessages messages) {
        return StylerCompilerExceptions.checkFail(messages);
    }

    public static StylerCompilerException compilerFail(Exception cause) {
        return withCause(StylerCompilerExceptions.compilerFail(cause), cause);
    }

    private static StylerCompilerException withCause(StylerCompilerException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public StylerCompilerExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
        return StylerCompilerExceptions.caseOf(this);
    }

    public Optional<KeyedMessages> getMessages() {
        return StylerCompilerExceptions.getMessages(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .mainFileFail((mainFile) -> "ESV main file '" + mainFile + "' does not exist or is not a file")
            .includeDirectoryFail((includeDirectory) -> "ESV include directory '" + includeDirectory + "' does not exist or is not a directory")
            .rootDirectoryFail((rootDirectory) -> "ESV root directory '" + rootDirectory + "' does not exist or is not a directory")
            .checkFail((messages) -> "Parsing or checking ESV source files failed")
            .compilerFail((cause) -> "ESV compiler failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
