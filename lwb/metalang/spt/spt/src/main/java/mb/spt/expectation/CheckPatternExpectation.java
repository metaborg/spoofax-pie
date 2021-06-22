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

public class CheckPatternExpectation implements TestExpectation {
    private final Severity severity;
    private final String like;
    private final ArrayList<SelectionReference> selectionReferences;
    private final Region sourceRegion;

    public CheckPatternExpectation(Severity severity, String like, ArrayList<SelectionReference> selectionReferences, Region sourceRegion) {
        this.severity = severity;
        this.like = like;
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
        final ArrayList<Message> messages = result.stream()
            .filter(m -> m.severity == severity)
            .filter(m -> m.text.contains(like))
            .filter(m -> m.region == null || testCase.testFragment.getRegion().contains(m.region))
            .collect(Collectors.toCollection(ArrayList::new));
        final String expected = severity.toDisplayString() + " message containing '" + like + "'";

        if(messages.isEmpty()) {
            addMessages = true;
            messagesBuilder.addMessage("Expected " + expected + ", but found none", Severity.Error, file, sourceRegion);
        }

        if(!CheckExpectationUtil.checkSelections(messages.stream(), selectionReferences, CheckCountExpectation.Operator.Equal, -1, expected, messagesBuilder, testCase)) {
            addMessages = true;
        }

        if(addMessages) {
            MessageRemap.addMessagesRemapped(messagesBuilder, testCase.resource, file, result);
        }

        return messagesBuilder.build(file);
    }
}
