package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
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
        @Nullable ResourcePath rootDirectoryHint,
        ExecContext context,
        CancelToken cancel
    ) throws InterruptedException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceKey file = testCase.testSuiteFile;

        final @Nullable CommandDef<?> commandDef = TransformExpectationUtil.getCommandDef(languageUnderTest,
            commandDisplayName, messagesBuilder, file, sourceRegion);
        if(commandDef == null) {
            return messagesBuilder.build(file);
        }

        if(!TransformExpectationUtil.isSelectionValid(testCase, selectionReference, messagesBuilder, file)) {
            return messagesBuilder.build(file);
        }
        final @Nullable Region selectionRegion = TransformExpectationUtil.getSelection(testCase, selectionReference);

        final @Nullable CommandFeedback feedback = TransformExpectationUtil.runCommand(testCase.resource, commandDef,
            languageUnderTest, languageUnderTestSession, messagesBuilder, file, rootDirectoryHint, sourceRegion, selectionRegion);
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
