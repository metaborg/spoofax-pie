package mb.spoofax.eclipse.menu;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import java.util.Map;

public abstract class MenuShared extends CompoundContributionItem implements IWorkbenchContribution {
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
}
