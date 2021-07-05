package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.spt.util.SptMessageRemap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class TransformToAtermExpectation implements TestExpectation {
    private final IStrategoTerm expectedMatch;
    private final String commandDisplayName;
    private final Region sourceRegion;

    public TransformToAtermExpectation(IStrategoTerm expectedMatch, String commandDisplayName, Region sourceRegion) {
        this.expectedMatch = expectedMatch;
        this.commandDisplayName = commandDisplayName;
        this.sourceRegion = sourceRegion;
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

        final @Nullable CommandDef<?> commandDef = TransformExpectationUtil.getCommandDef(languageUnderTest, commandDisplayName, messagesBuilder, file, sourceRegion);
        if(commandDef == null) {
            return messagesBuilder.build(file);
        }

        final @Nullable CommandFeedback feedback = TransformExpectationUtil.runCommand(testCase.resource, commandDef, languageUnderTest, languageUnderTestSession, messagesBuilder, file, sourceRegion);
        if(feedback == null) {
            return messagesBuilder.build(file);
        }

        if(feedback.hasException()) {
            messagesBuilder.addMessage("Expected executing command '" + commandDef + "' to succeed, but it threw an exception", feedback.getException(), Severity.Error, file, sourceRegion);
        } else if(feedback.hasErrorMessages()) {
            messagesBuilder.addMessage("Expected executing command '" + commandDef + "' to succeed, but it returned error messages", Severity.Error, file, sourceRegion);
        } else {
            // TODO: check that `feedback` matches `expectedMatch`.
        }

        if(feedback.hasErrorMessages()) {
            SptMessageRemap.addMessagesRemapped(messagesBuilder, testCase.resource, file, feedback.getMessages());
        }

        return messagesBuilder.build(file);
    }
}
