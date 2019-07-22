package mb.spoofax.eclipse.menu;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.common.util.SerializationUtil;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.menu.MenuItemVisitor;
import mb.spoofax.core.language.transform.*;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.transform.TransformData;
import mb.spoofax.eclipse.transform.TransformHandler;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

abstract class MenuShared extends CompoundContributionItem implements IWorkbenchContribution {
    protected @MonotonicNonNull IServiceLocator serviceLocator;


    @Override public void initialize(@NonNull IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }


    protected CommandContributionItem command(String commandId, @Nullable String label, @Nullable Map<String, String> parameters, int style) {
        final CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator, null, commandId, style);
        p.label = label;
        p.parameters = parameters;
        return new CommandContributionItem(p);
    }

    protected CommandContributionItem command(String commandId, @Nullable String label, @Nullable Map<String, String> parameters) {
        return command(commandId, label, parameters, CommandContributionItem.STYLE_PUSH);
    }

    protected CommandContributionItem command(String commandId, @Nullable String label) {
        return command(commandId, label, null);
    }

    protected CommandContributionItem command(String commandId) {
        return command(commandId, null);
    }


    protected CommandContributionItem transformCommand(String commandId, TransformRequest transformRequest, ListView<TransformInput> inputs, String displayName) {
        final TransformData data = new TransformData(transformRequest.transformDef.getId(), transformRequest.executionType, inputs);
        final Map<String, String> parameters = new HashMap<>();
        final String serialized = SerializationUtil.serializeToString(data);
        parameters.put(TransformHandler.dataParameterId, serialized);
        return command(commandId, displayName, parameters);
    }
}
