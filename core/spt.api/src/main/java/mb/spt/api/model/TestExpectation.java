package mb.spt.api.model;

import mb.common.message.KeyedMessages;
import mb.pie.api.Session;

public interface TestExpectation {
    KeyedMessages evaluate(LanguageUnderTest languageUnderTest, Session session, TestCase testCase) throws InterruptedException;
}
