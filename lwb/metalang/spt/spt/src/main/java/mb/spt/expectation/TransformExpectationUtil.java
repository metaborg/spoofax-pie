package mb.spt.expectation;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.pie.api.ExecException;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spt.model.LanguageUnderTest;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TransformExpectationUtil {
    public static @Nullable CommandDef<?> getCommandDef(
        LanguageUnderTest languageUnderTest,
        String commandDisplayName,
        KeyedMessagesBuilder messagesBuilder,
        ResourceKey failMessageFile,
        Region fileMessageRegion
    ) {
        final LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();
        final @Nullable CommandDef<?> commandDef = languageInstance.getCommandDefs().stream()
            .filter(cd -> cd.getDisplayName().equals(commandDisplayName))
            .findAny()
            .orElse(null);
        if(commandDef == null) {
            messagesBuilder.addMessage("Command definition with display name '" + commandDisplayName + "' was not found", Severity.Error, failMessageFile, fileMessageRegion);
        }
        return commandDef;
    }

    public static @Nullable CommandFeedback runCommand(
        ResourceKey resource,
        CommandDef<?> commandDef,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        KeyedMessagesBuilder messagesBuilder,
        ResourceKey failMessageFile,
        Region fileMessageRegion
    ) throws InterruptedException {
        final CommandContext commandContext = CommandContext.ofReadableResource(resource);
        final Task<CommandFeedback> task = commandDef.createTask(CommandExecutionType.ManualOnce, commandContext, new ArgConverters(languageUnderTest.getResourceServiceComponent().getResourceService()));
        try {
            return languageUnderTestSession.require(task);
        } catch(ExecException e) {
            messagesBuilder.addMessage("Failed to execute command '" + commandDef + "'; see exception", e, Severity.Error, failMessageFile, fileMessageRegion);
            return null;
        }
    }
}
