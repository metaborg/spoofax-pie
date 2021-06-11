package mb.spt.api.model;

import mb.common.message.KeyedMessagesBuilder;
import mb.pie.api.ExecContext;
import mb.spoofax.core.language.LanguageComponent;

public interface TestExpectation {
    boolean evaluate(ExecContext context, LanguageComponent languageComponent, KeyedMessagesBuilder messagesBuilder, TestCase testCase);
}
