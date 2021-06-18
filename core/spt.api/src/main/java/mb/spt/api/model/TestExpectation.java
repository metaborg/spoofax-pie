package mb.spt.api.model;

import mb.common.message.KeyedMessages;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;

public interface TestExpectation {
    KeyedMessages evaluate(LanguageUnderTest languageUnderTest, Session session, CancelToken cancel, TestCase testCase) throws InterruptedException;
}
