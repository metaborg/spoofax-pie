package mb.spoofax.lwb.compiler.statix;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

@ADT
public abstract class StatixCompileException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R mainFileFail(ResourceKey mainFile);

        R sourceDirectoryFail(ResourcePath rootDirectory);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R checkFail(KeyedMessages messages);

        R compilerFail(Exception cause);
    }

    public static StatixCompileException mainFileFail(ResourceKey mainFile) {
        return StatixCompileExceptions.mainFileFail(mainFile);
    }

    public static StatixCompileException sourceDirectoryFail(ResourcePath rootDirectory) {
        return StatixCompileExceptions.sourceDirectoryFail(rootDirectory);
    }

    public static StatixCompileException includeDirectoryFail(ResourcePath includeDirectory) {
        return StatixCompileExceptions.includeDirectoryFail(includeDirectory);
    }

    public static StatixCompileException checkFail(KeyedMessages messages) {
        return StatixCompileExceptions.checkFail(messages);
    }

    public static StatixCompileException compilerFail(Exception cause) {
        return withCause(StatixCompileExceptions.compilerFail(cause), cause);
    }

    private static StatixCompileException withCause(StatixCompileException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public StatixCompileExceptions.CasesMatchers.TotalMatcher_MainFileFail cases() {
        return StatixCompileExceptions.cases();
    }

    public StatixCompileExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
        return StatixCompileExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .mainFileFail((mainFile) -> "Statix main file '" + mainFile + "' does not exist or is not a file")
            .sourceDirectoryFail((rootDirectory) -> "Statix root directory '" + rootDirectory + "' does not exist or is not a directory")
            .includeDirectoryFail((includeDirectory) -> "Statix include directory '" + includeDirectory + "' does not exist or is not a directory")
            .checkFail((messages) -> "Parsing or checking Statix source files failed")
            .compilerFail((cause) -> "Statix compiler failed unexpectedly")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return StatixCompileExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
