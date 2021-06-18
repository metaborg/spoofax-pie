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
import mb.spt.util.MessageRemap;

public class CheckCountExpectation implements TestExpectation {
    public enum Operator {
        Equal, Less, LessOrEqual, More, MoreOrEqual
    }

    private final Operator operator;
    private final long expectedCount;
    private final Severity severity;
    private final Region sourceRegion;

    public CheckCountExpectation(Operator operator, long expectedCount, Severity severity, Region sourceRegion) {
        this.operator = operator;
        this.expectedCount = expectedCount;
        this.severity = severity;
        this.sourceRegion = sourceRegion;
    }

    @Override
    public KeyedMessages evaluate(LanguageUnderTest languageUnderTest, Session session, CancelToken cancel, TestCase testCase) throws InterruptedException {
        final ResourceKey file = testCase.testSuiteFile;
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        try {
            final KeyedMessages result = session.requireWithoutObserving(languageUnderTest.getLanguageComponent().getLanguageInstance().createCheckOneTask(testCase.resource, testCase.rootDirectoryHint), cancel);
            final long count = result.getMessagesOfSeverity(severity).count();
            if(!test(count)) {
                MessageRemap.addMessagesRemapped(messagesBuilder, testCase.resource, file, result);
                messagesBuilder.addMessage("Expected " + operatorString(operator) + " " + expectedCount + " " + severity.toDisplayString() + " message(s), but got " + count, Severity.Error, file, sourceRegion);
            }
        } catch(ExecException e) {
            messagesBuilder.addMessage("Failed to evaluate check expectation; see exception", e, Severity.Error, file, sourceRegion);
        }
        return messagesBuilder.build(file);
    }

    private boolean test(long count) {
        switch(operator) {
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

    private String operatorString(Operator operator) {
        switch(operator) {
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
