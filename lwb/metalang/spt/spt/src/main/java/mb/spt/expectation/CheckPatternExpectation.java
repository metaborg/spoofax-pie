package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.Severity;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.spt.api.model.LanguageUnderTest;
import mb.spt.api.model.TestCase;
import mb.spt.api.model.TestExpectation;

public class CheckPatternExpectation implements TestExpectation {
    private final Severity severity;
    private final String like;

    public CheckPatternExpectation(Severity severity, String like) {
        this.severity = severity;
        this.like = like;
    }

    @Override
    public KeyedMessages evaluate(LanguageUnderTest languageUnderTest, Session session, CancelToken cancel, TestCase testCase) throws InterruptedException {
        return KeyedMessages.of(); // TODO
    }
}
