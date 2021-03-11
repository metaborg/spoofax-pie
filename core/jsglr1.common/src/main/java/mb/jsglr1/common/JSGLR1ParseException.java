package mb.jsglr1.common;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Optional;

@ADT
public abstract class JSGLR1ParseException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R readStringFail(String source, IOException cause);

        R parseFail(KeyedMessages messages);

        R recoveryDisallowedFail(KeyedMessages messages);
    }

    public static JSGLR1ParseException readStringFail(String source, IOException cause) {
        final JSGLR1ParseException e = JSGLR1ParseExceptions.readStringFail(source, cause);
        e.initCause(cause);
        return e;
    }

    public static JSGLR1ParseException parseFail(KeyedMessages messages) {
        return JSGLR1ParseExceptions.parseFail(messages);
    }

    public static JSGLR1ParseException recoveryDisallowedFail(KeyedMessages messages) {
        return JSGLR1ParseExceptions.recoveryDisallowedFail(messages);
    }


    public abstract <R> R match(Cases<R> cases);

    public JSGLR1ParseExceptions.CasesMatchers.TotalMatcher_ReadStringFail cases() {
        return JSGLR1ParseExceptions.cases();
    }

    public JSGLR1ParseExceptions.CaseOfMatchers.TotalMatcher_ReadStringFail caseOf() {
        return JSGLR1ParseExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .readStringFail((source, cause) -> "Parsing failed; cannot get text to parse from '" + source + "'")
            .parseFail((messages) -> "Parsing failed; see error messages")
            .recoveryDisallowedFail((messages) -> "Parsing recovered from failure, but recovery was disallowed; see error messages");
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
