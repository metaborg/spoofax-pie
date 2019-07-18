package mb.spoofax.eclipse.menu;

import mb.common.util.ListView;
import mb.common.util.SerializationUtil;
import mb.spoofax.core.language.transform.TransformInput;
import mb.spoofax.core.language.transform.TransformRequest;
import mb.spoofax.eclipse.transform.TransformData;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractMenu extends CompoundContributionItem implements IWorkbenchContribution {
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
        parameters.put("input", new String(SerializationUtil.serialize(data), StandardCharsets.UTF_8));
        return command(commandId, displayName, parameters);
    }
}
