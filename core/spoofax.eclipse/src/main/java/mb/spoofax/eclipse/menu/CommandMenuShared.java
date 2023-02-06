package mb.spoofax.eclipse.menu;

import mb.common.util.ListView;
import mb.common.util.SerializationUtil;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.eclipse.command.CommandData;
import mb.spoofax.eclipse.command.RunCommandHandler;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;

import java.util.HashMap;
import java.util.Map;

abstract class CommandMenuShared extends MenuShared implements IWorkbenchContribution {
    protected CommandContributionItem createCommand(
        String commandId,
        Coordinate languageCoordinate,
        CommandRequest<?> commandRequest,
        CommandContext context,
        String displayName,
        String description
    ) {
        return createCommand(commandId, languageCoordinate, commandRequest, ListView.of(context), displayName, description);
    }

    protected CommandContributionItem createCommand(
        String commandId,
        Coordinate languageCoordinate,
        CommandRequest<?> commandRequest,
        ListView<? extends CommandContext> contexts,
        String displayName,
        String description
    ) {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(RunCommandHandler.languageCoordinateParameterId, languageCoordinate.toString());
        final CommandData data = new CommandData(commandRequest, contexts);
        final String serialized = SerializationUtil.serializeToString(data);
        parameters.put(RunCommandHandler.dataParameterId, serialized);
        return createCommand(commandId, displayName, description, parameters);
    }
}
