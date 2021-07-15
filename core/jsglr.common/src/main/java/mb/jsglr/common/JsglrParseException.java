package mb.jsglr.common;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Optional;

@ADT
public abstract class JsglrParseException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R readStringFail(IOException ioException, Option<String> startSymbol, Option<ResourceKey> fileHint, Option<ResourcePath> rootDirectoryHint);

        R parseFail(KeyedMessages messages, Option<String> startSymbol, Option<ResourceKey> fileHint, Option<ResourcePath> rootDirectoryHint);

        R recoveryDisallowedFail(KeyedMessages messages, Option<String> startSymbol, Option<ResourceKey> fileHint, Option<ResourcePath> rootDirectoryHint);

        R otherFail(Exception cause, Option<String> startSymbol, Option<ResourceKey> fileHint, Option<ResourcePath> rootDirectoryHint);
    }

    public static JsglrParseException readStringFail(IOException cause, Option<String> startSymbol, Option<ResourceKey> fileHint, Option<ResourcePath> rootDirectoryHint) {
        final JsglrParseException e = JsglrParseExceptions.readStringFail(cause, startSymbol, fileHint, rootDirectoryHint);
        e.initCause(cause);
        return e;
    }

    public static JsglrParseException readStringFail(IOException ioException, @Nullable String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        return readStringFail(ioException, Option.ofNullable(startSymbol), Option.ofNullable(fileHint), Option.ofNullable(rootDirectoryHint));
    }

    public static JsglrParseException parseFail(KeyedMessages messages, Option<String> startSymbol, Option<ResourceKey> fileHint, Option<ResourcePath> rootDirectoryHint) {
        return JsglrParseExceptions.parseFail(messages, startSymbol, fileHint, rootDirectoryHint);
    }

    public static JsglrParseException parseFail(KeyedMessages messages, @Nullable String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        return parseFail(messages, Option.ofNullable(startSymbol), Option.ofNullable(fileHint), Option.ofNullable(rootDirectoryHint));
    }

    public static JsglrParseException recoveryDisallowedFail(KeyedMessages messages, Option<String> startSymbol, Option<ResourceKey> fileHint, Option<ResourcePath> rootDirectoryHint) {
        return JsglrParseExceptions.recoveryDisallowedFail(messages, startSymbol, fileHint, rootDirectoryHint);
    }

    public static JsglrParseException recoveryDisallowedFail(KeyedMessages messages, @Nullable String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        return recoveryDisallowedFail(messages, Option.ofNullable(startSymbol), Option.ofNullable(fileHint), Option.ofNullable(rootDirectoryHint));
    }

    public static JsglrParseException otherFail(Exception cause, Option<String> startSymbol, Option<ResourceKey> fileHint, Option<ResourcePath> rootDirectoryHint) {
        return JsglrParseExceptions.otherFail(cause, startSymbol, fileHint, rootDirectoryHint);
    }

    public static JsglrParseException otherFail(Exception cause, @Nullable String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        return otherFail(cause, Option.ofNullable(startSymbol), Option.ofNullable(fileHint), Option.ofNullable(rootDirectoryHint));
    }


    public abstract <R> R match(Cases<R> cases);

    public JsglrParseExceptions.CasesMatchers.TotalMatcher_ReadStringFail cases() {
        return JsglrParseExceptions.cases();
    }

    public JsglrParseExceptions.CaseOfMatchers.TotalMatcher_ReadStringFail caseOf() {
        return JsglrParseExceptions.caseOf(this);
    }

    public Option<String> getStartSymbol() {
        return JsglrParseExceptions.getStartSymbol(this);
    }

    public Option<ResourceKey> getFileHint() {
        return JsglrParseExceptions.getFileHint(this);
    }

    public Option<ResourcePath> getRootDirectoryHint() {
        return JsglrParseExceptions.getRootDirectoryHint(this);
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
        return JsglrParseExceptions.getMessages(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
