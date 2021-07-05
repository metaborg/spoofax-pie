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

public class TransformToFragmentExpectation implements TestExpectation {
    private final ResourceKey fragmentResource;
    private final @Nullable String languageIdHint;
    private final String commandDisplayName;
    private final Region sourceRegion;

    public TransformToFragmentExpectation(ResourceKey fragmentResource, @Nullable String languageIdHint, String commandDisplayName, Region sourceRegion) {
        this.fragmentResource = fragmentResource;
        this.languageIdHint = languageIdHint;
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

        final @Nullable LanguageUnderTest fragmentLanguageUnderTest = ExpectationFragmentUtil.getLanguageUnderTest(testCase, languageUnderTest, languageUnderTestProvider, context, languageIdHint);
        if(fragmentLanguageUnderTest == null) {
            messagesBuilder.addMessage("Cannot evaluate parse to fragment expectation because providing language under test for language id '" + languageIdHint + "' failed unexpectedly", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }
        final @Nullable CommandDef<?> fragmentCommandDef = TransformExpectationUtil.getCommandDef(fragmentLanguageUnderTest, commandDisplayName, messagesBuilder, file, sourceRegion);
        if(fragmentCommandDef == null) {
            return messagesBuilder.build(file);
        }
        final @Nullable CommandFeedback fragmentFeedback = TransformExpectationUtil.runCommand(fragmentResource, commandDef, fragmentLanguageUnderTest, fragmentLanguageUnderTest.getPieComponent().newSession() /* OPTO: share a single session for one test suite run. */, messagesBuilder, file, sourceRegion);
        if(fragmentFeedback == null) {
            return messagesBuilder.build(file);
        }

        if(feedback.hasException()) {
            messagesBuilder.addMessage("Expected executing command '" + commandDef + "' to succeed, but it threw an exception", feedback.getException(), Severity.Error, file, sourceRegion);
        } else if(feedback.hasErrorMessages()) {
            messagesBuilder.addMessage("Expected executing command '" + commandDef + "' to succeed, but it returned error messages", Severity.Error, file, sourceRegion);
        } else {
            // TODO: check that `feedback` matches `fragmentFeedback`.
        }

        if(feedback.hasErrorMessages()) {
            SptMessageRemap.addMessagesRemapped(messagesBuilder, testCase.resource, file, feedback.getMessages());
        }

        return messagesBuilder.build(file);
    }
}
