package mb.spt.expectation;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.ListView;
import mb.pie.api.ExecException;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofax.core.language.command.arg.ArgumentBuilderException;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
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
        ResourcePath resource,
        CommandDef<?> commandDef,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        KeyedMessagesBuilder messagesBuilder,
        ResourceKey failMessageFile,
        @Nullable ResourcePath rootDirectoryHint,
        Region fileMessageRegion,
        @Nullable Region selection
    ) throws InterruptedException {
        try {
            // Using `ofFile` is required to make `transform` test expectations work.
            // Otherwise, the argument provider `Context(File)` will not pick up this resource.
            final CommandContext commandContext = CommandContext.ofFile(resource, selection);
            commandContext.setEnclosing(EnclosingCommandContextType.Directory, CommandContext.ofDirectory(resource));
            // If no root directory hint is given, use the resource itself as the project context.
            commandContext.setEnclosing(EnclosingCommandContextType.Project, CommandContext.ofProject(rootDirectoryHint != null ? rootDirectoryHint : resource));
            final Task<CommandFeedback> task = commandDef.createTask(CommandExecutionType.ManualOnce, commandContext, new ArgConverters(languageUnderTest.getResourceServiceComponent().getResourceService()));
            return languageUnderTestSession.require(task);
        } catch(ExecException | ArgumentBuilderException e) {
            messagesBuilder.addMessage("Failed to execute command '" + commandDef + "'; see exception", e, Severity.Error, failMessageFile, fileMessageRegion);
            return null;
        }
    }

    public static boolean isSelectionValid(TestCase testCase, Option<SelectionReference> selectionReference,
        KeyedMessagesBuilder messagesBuilder, ResourceKey file) {
        if(selectionReference.isSome()) {
            final SelectionReference ref = selectionReference.get();
            final int count = testCase.testFragment.getInFragmentSelections().size();
            if(ref.selection > count) {
                messagesBuilder.addMessage("Cannot resolve #" + ref.selection + ". Only " + count + " available.",
                    Severity.Error, file, ref.region);
                return false;
            }
        }
         return true;
    }

    public static @Nullable Region getSelection(TestCase testCase, Option<SelectionReference> selectionReference) {
        if(selectionReference.isNone()) {
            return null;
        } else {
            // `getSelections()` returns the mapped regions, as the object language parser sees them.
            // This corresponds to the regions Stratego strategies such as `origin-offset` etc. return.
            final ListView<Region> availableSelections = testCase.testFragment.getSelections();
            return availableSelections.get(selectionReference.get().selection - 1);
        }
    }
}
