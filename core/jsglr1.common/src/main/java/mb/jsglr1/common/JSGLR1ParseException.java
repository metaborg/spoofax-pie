package mb.jsglr1.common;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Optional;

@ADT @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class JSGLR1ParseException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R readStringFail(IOException ioException, Optional<String> startSymbol, Optional<ResourceKey> fileHint, Optional<ResourcePath> rootDirectoryHint);

        R parseFail(KeyedMessages messages, Optional<String> startSymbol, Optional<ResourceKey> fileHint, Optional<ResourcePath> rootDirectoryHint);

        R recoveryDisallowedFail(KeyedMessages messages, Optional<String> startSymbol, Optional<ResourceKey> fileHint, Optional<ResourcePath> rootDirectoryHint);

        R otherFail(Exception cause, Optional<String> startSymbol, Optional<ResourceKey> fileHint, Optional<ResourcePath> rootDirectoryHint);
    }

    public static JSGLR1ParseException readStringFail(IOException cause, Optional<String> startSymbol, Optional<ResourceKey> fileHint, Optional<ResourcePath> rootDirectoryHint) {
        final JSGLR1ParseException e = JSGLR1ParseExceptions.readStringFail(cause, startSymbol, fileHint, rootDirectoryHint);
        e.initCause(cause);
        return e;
    }

    public static JSGLR1ParseException readStringFail(IOException ioException, @Nullable String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        return readStringFail(ioException, Optional.ofNullable(startSymbol), Optional.ofNullable(fileHint), Optional.ofNullable(rootDirectoryHint));
    }

    public static JSGLR1ParseException parseFail(KeyedMessages messages, Optional<String> startSymbol, Optional<ResourceKey> fileHint, Optional<ResourcePath> rootDirectoryHint) {
        return JSGLR1ParseExceptions.parseFail(messages, startSymbol, fileHint, rootDirectoryHint);
    }

    public static JSGLR1ParseException parseFail(KeyedMessages messages, @Nullable String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        return parseFail(messages, Optional.ofNullable(startSymbol), Optional.ofNullable(fileHint), Optional.ofNullable(rootDirectoryHint));
    }

    public static JSGLR1ParseException recoveryDisallowedFail(KeyedMessages messages, Optional<String> startSymbol, Optional<ResourceKey> fileHint, Optional<ResourcePath> rootDirectoryHint) {
        return JSGLR1ParseExceptions.recoveryDisallowedFail(messages, startSymbol, fileHint, rootDirectoryHint);
    }

    public static JSGLR1ParseException recoveryDisallowedFail(KeyedMessages messages, @Nullable String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        return recoveryDisallowedFail(messages, Optional.ofNullable(startSymbol), Optional.ofNullable(fileHint), Optional.ofNullable(rootDirectoryHint));
    }

    public static JSGLR1ParseException otherFail(Exception cause, Optional<String> startSymbol, Optional<ResourceKey> fileHint, Optional<ResourcePath> rootDirectoryHint) {
        return JSGLR1ParseExceptions.otherFail(cause, startSymbol, fileHint, rootDirectoryHint);
    }

    public static JSGLR1ParseException otherFail(Exception cause, @Nullable String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        return otherFail(cause, Optional.ofNullable(startSymbol), Optional.ofNullable(fileHint), Optional.ofNullable(rootDirectoryHint));
    }


    public abstract <R> R match(Cases<R> cases);

    public JSGLR1ParseExceptions.CasesMatchers.TotalMatcher_ReadStringFail cases() {
        return JSGLR1ParseExceptions.cases();
    }

    public JSGLR1ParseExceptions.CaseOfMatchers.TotalMatcher_ReadStringFail caseOf() {
        return JSGLR1ParseExceptions.caseOf(this);
    }

    public Optional<String> getStartSymbol() {
        return JSGLR1ParseExceptions.getStartSymbol(this);
    }

    public Optional<ResourceKey> getFileHint() {
        return JSGLR1ParseExceptions.getFileHint(this);
    }

    public Optional<ResourcePath> getRootDirectoryHint() {
        return JSGLR1ParseExceptions.getRootDirectoryHint(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .readStringFail((source, startSymbol, fileHint, rootDirectoryHint) -> "Parsing failed; cannot get text to parse from '" + fileHint + "'")
            .parseFail((messages, startSymbol, fileHint, rootDirectoryHint) -> "Parsing failed; see error messages")
            .recoveryDisallowedFail((messages, startSymbol, fileHint, rootDirectoryHint) -> "Parsing recovered from failure, but recovery was disallowed; see error messages")
            .otherFail((cause, startSymbol, fileHint, rootDirectoryHint) -> "Parsing failed unexpectedly; see cause")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return JSGLR1ParseExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
