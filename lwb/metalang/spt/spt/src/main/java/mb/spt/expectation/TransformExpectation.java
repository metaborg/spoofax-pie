package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.spt.util.SptMessageRemap;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TransformExpectation implements TestExpectation {
    private final String commandDisplayName;
    private final Region sourceRegion;
    private final Option<SelectionReference> selectionReference;

    public TransformExpectation(String commandDisplayName, Region sourceRegion,
        Option<SelectionReference> selectionReference) {
        this.commandDisplayName = commandDisplayName;
        this.sourceRegion = sourceRegion;
        this.selectionReference = selectionReference;
    }

    @Override
    public KeyedMessages evaluate(
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        CancelToken cancel
    ) throws InterruptedException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceKey file = testCase.testSuiteFile;

        final @Nullable Region selection;
        if(selectionReference.isNone()) {
            selection = null;
        } else {
            final SelectionReference selectionReference = this.selectionReference.get();
            final Integer selectionIndex = selectionReference.selection;
            final ListView<Region> availableSelections = testCase.testFragment.getSelections();
            if(selectionIndex > availableSelections.size()) {
                messagesBuilder.addMessage("Cannot resolve #" + selectionIndex + ". Only " + availableSelections.size() + " available.",
                    Severity.Error, file, selectionReference.region);
                return messagesBuilder.build(file);
            }
            selection = availableSelections.get(selectionIndex - 1);
        }

        final @Nullable CommandDef<?> commandDef = TransformExpectationUtil.getCommandDef(languageUnderTest,
            commandDisplayName, messagesBuilder, file, sourceRegion);
        if(commandDef == null) {
            return messagesBuilder.build(file);
        }

        final @Nullable CommandFeedback feedback = TransformExpectationUtil.runCommand(testCase.resource, commandDef,
            languageUnderTest, languageUnderTestSession, messagesBuilder, file, sourceRegion, selection);
        if(feedback == null) {
            return messagesBuilder.build(file);
        }

        if(feedback.hasException()) {
            messagesBuilder.addMessage("Expected executing command '" + commandDef + "' to succeed, but it threw an exception", feedback.getException(), Severity.Error, file, sourceRegion);
        } else if(feedback.hasErrorMessages()) {
            messagesBuilder.addMessage("Expected executing command '" + commandDef + "' to succeed, but it returned error messages", Severity.Error, file, sourceRegion);
        }

        if(feedback.hasErrorMessages()) {
            SptMessageRemap.addMessagesRemapped(messagesBuilder, testCase, file, sourceRegion, feedback.getMessages());
        }

        return messagesBuilder.build(file);
    }
}
