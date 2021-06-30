package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.spt.util.SptMessageRemap;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TransformExpectation implements TestExpectation {
    private final String commandDisplayName;
    private final Region sourceRegion;

    public TransformExpectation(String commandDisplayName, Region sourceRegion) {
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
        final LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();
        final @Nullable CommandDef<?> commandDef = languageInstance.getCommandDefs().stream()
            .filter(cd -> cd.getDisplayName().equals(commandDisplayName))
            .findAny()
            .orElse(null);
        if(commandDef == null) {
            messagesBuilder.addMessage("Command definition with display name '" + commandDisplayName + "' was not found", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }

        final CommandContext commandContext = CommandContext.ofResource(testCase.resource);
        final Task<CommandFeedback> task = commandDef.createTask(CommandExecutionType.ManualOnce, commandContext, new ArgConverters(languageUnderTest.getResourceServiceComponent().getResourceService()));
        final CommandFeedback feedback;
        try {
            feedback = languageUnderTestSession.require(task);
        } catch(ExecException e) {
            messagesBuilder.addMessage("Failed to execute command '" + commandDef + "'; see exception", e, Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }

        if(feedback.hasException()) {
            messagesBuilder.addMessage("Expected executing command '" + commandDef + "' to succeed, but it threw an exception", feedback.getException(), Severity.Error, file, sourceRegion);
        } else if(feedback.hasErrorMessages()) {
            messagesBuilder.addMessage("Expected executing command '" + commandDef + "' to succeed, but it returned error messages", Severity.Error, file, sourceRegion);
        }

        if(feedback.hasErrorMessages()) {
            SptMessageRemap.addMessagesRemapped(messagesBuilder, testCase.resource, file, feedback.getMessages());
        }

        return messagesBuilder.build(file);
    }
}
