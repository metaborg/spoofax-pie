package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.pie.api.ExecException;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spt.api.model.LanguageUnderTest;
import mb.spt.api.model.TestCase;
import mb.spt.api.model.TestExpectation;
import mb.spt.model.SelectionReference;
import mb.spt.util.MessageRemap;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CheckCountExpectation implements TestExpectation {
    public enum Operator {
        Equal, Less, LessOrEqual, More, MoreOrEqual;

        public boolean test(long count, long expectedCount) {
            switch(this) {
                default:
                case Equal:
                    return count == expectedCount;
                case Less:
                    return count < expectedCount;
                case LessOrEqual:
                    return count <= expectedCount;
                case More:
                    return count > expectedCount;
                case MoreOrEqual:
                    return count >= expectedCount;
            }
        }

        @Override public String toString() {
            switch(this) {
                default:
                case Equal:
                    return "==";
                case Less:
                    return "<";
                case LessOrEqual:
                    return "<=";
                case More:
                    return ">";
                case MoreOrEqual:
                    return ">=";
            }
        }
    }

    private final Operator operator;
    private final long expectedCount;
    private final Severity severity;
    private final ArrayList<SelectionReference> selectionReferences;
    private final Region sourceRegion;

    public CheckCountExpectation(Operator operator, long expectedCount, Severity severity, ArrayList<SelectionReference> selectionReferences, Region sourceRegion) {
        this.operator = operator;
        this.expectedCount = expectedCount;
        this.severity = severity;
        this.selectionReferences = selectionReferences;
        this.sourceRegion = sourceRegion;
    }

    @Override
    public KeyedMessages evaluate(LanguageUnderTest languageUnderTest, Session session, CancelToken cancel, TestCase testCase) throws InterruptedException {
        final ResourceKey file = testCase.testSuiteFile;
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final KeyedMessages result;
        try {
            result = session.requireWithoutObserving(languageUnderTest.getLanguageComponent().getLanguageInstance().createCheckOneTask(testCase.resource, testCase.rootDirectoryHint), cancel);
        } catch(ExecException e) {
            messagesBuilder.addMessage("Failed to evaluate check expectation; see exception", e, Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }

        boolean addMessages = false;
        final ArrayList<Message> messages = result
            .getMessagesOfSeverity(severity)
            .filter(m -> m.region == null || testCase.testFragment.getRegion().contains(m.region))
            .collect(Collectors.toCollection(ArrayList::new));
        final String expected = operator + " " + expectedCount + " " + severity.toDisplayString();

        if(selectionReferences.isEmpty()) {
            final long count = messages.size();
            if(!operator.test(count, expectedCount)) {
                addMessages = true;
                messagesBuilder.addMessage("Expected " + expected + " message(s), but got " + count, Severity.Error, file, sourceRegion);
            }
        }

        if(!CheckExpectationUtil.checkSelections(messages.stream(), selectionReferences, operator, expectedCount, expected, messagesBuilder, testCase)) {
            addMessages = true;
        }

        if(addMessages) {
            MessageRemap.addMessagesRemapped(messagesBuilder, testCase.resource, file, result);
        }

        return messagesBuilder.build(file);
    }
}
