package mb.spoofax.eclipse.menu;

import mb.common.util.ListView;
import mb.common.util.SerializationUtil;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.eclipse.command.CommandData;
import mb.spoofax.eclipse.command.RunCommandHandler;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import java.util.HashMap;
import java.util.Map;

abstract class MenuShared extends CompoundContributionItem implements IWorkbenchContribution {
    private @MonotonicNonNull IServiceLocator serviceLocator;


    @Override public void initialize(@NonNull IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }


    protected CommandContributionItem createCommand(
        String commandId,
        @Nullable String label,
        @Nullable String tooltip,
        @Nullable Map<String, String> parameters,
        int style
    ) {
        final CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator, null, commandId, style);
        p.label = label;
        p.tooltip = tooltip;
        p.parameters = parameters;
        return new CommandContributionItem(p);
    }

    protected CommandContributionItem createCommand(String commandId, @Nullable String label, @Nullable String tooltip, @Nullable Map<String, String> parameters) {
        return createCommand(commandId, label, tooltip, parameters, CommandContributionItem.STYLE_PUSH);
    }

    protected CommandContributionItem createCommand(String commandId, @Nullable String label, @Nullable String tooltip) {
        return createCommand(commandId, label, tooltip, null);
    }

    protected CommandContributionItem createCommand(String commandId, @Nullable String label) {
        return createCommand(commandId, label, null);
    }

    protected CommandContributionItem createCommand(String commandId) {
        return createCommand(commandId, null);
    }


    protected CommandContributionItem createCommand(String commandId, CommandRequest<?> commandRequest, CommandContext context, String displayName, String description) {
        return createCommand(commandId, commandRequest, ListView.of(context), displayName, description);
    }

    protected CommandContributionItem createCommand(String commandId, CommandRequest<?> commandRequest, ListView<? extends CommandContext> contexts, String displayName, String description) {
        final CommandData data = new CommandData(commandRequest, contexts);
        final Map<String, String> parameters = new HashMap<>();
        final String serialized = SerializationUtil.serializeToString(data);
        parameters.put(RunCommandHandler.dataParameterId, serialized);
        return createCommand(commandId, displayName, description, parameters);
    }
}
