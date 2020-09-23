package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class ParserCompilerException extends Exception {
    public interface Cases<R> {
        R mainFileFail(ResourceKey mainFile);

        R rootDirectoryFail(ResourcePath rootDirectory);

        R checkFail(KeyedMessages messages);

        R createParseTableFail(Exception cause);
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

    public static ParserCompilerException createParseTableFail(Exception cause) {
        final ParserCompilerException e = ParserCompilerExceptions.createParseTableFail(cause);
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
            .checkFail((messages) -> "Parsing or checking the syntax specification failed; see error messages")
            .createParseTableFail((cause) -> "Creating the parse table failed; see cause")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
