package mb.jsglr1.common;

import mb.common.message.Messages;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Optional;

@ADT
public abstract class JSGLR1ParseException extends Exception {
    public interface Cases<R> {
        R readStringFail(String source, IOException cause);

        R parseFail(Messages messages);

        R recoveryDisallowedFail(Messages messages);
    }

    public static JSGLR1ParseException readStringFail(String source, IOException cause) {
        return JSGLR1ParseExceptions.readStringFail(source, cause);
    }

    public static JSGLR1ParseException parseFail(Messages messages) {
        return JSGLR1ParseExceptions.parseFail(messages);
    }

    public static JSGLR1ParseException recoveryDisallowedFail(Messages messages) {
        return JSGLR1ParseExceptions.recoveryDisallowedFail(messages);
    }


    public abstract <R> R match(Cases<R> cases);

    public JSGLR1ParseExceptions.CaseOfMatchers.TotalMatcher_ReadStringFail caseOf() {
        return JSGLR1ParseExceptions.caseOf(this);
    }

    public Optional<Messages> getMessages() {
        return JSGLR1ParseExceptions.getMessages(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .readStringFail((source, cause) -> "Parsing failed; cannot get text to parse from '" + source + "'")
            .parseFail((messages) -> "Parsing failed; see error messages")
            .recoveryDisallowedFail((messages) -> "Parsing recovered from failure, but recovery was disallowed; see error messages");
    }

    @Override public synchronized Throwable getCause() {
        return caseOf()
            .readStringFail((source, cause) -> cause)
            .parseFail((messages) -> null)
            .recoveryDisallowedFail((messages) -> null);
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();


    protected JSGLR1ParseException() {
        super(null, null, true, false);
    }
}
