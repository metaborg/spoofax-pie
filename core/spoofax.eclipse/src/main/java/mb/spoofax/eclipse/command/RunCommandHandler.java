package mb.spoofax.eclipse.command;

import mb.common.util.MapView;
import mb.common.util.SerializationUtil;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import java.util.HashMap;

public class RunCommandHandler extends AbstractHandler {
    public final static String dataParameterId = "data";

    private final EclipseLanguageComponent languageComponent;

    private final PieRunner pieRunner;

    private final MapView<String, CommandDef> commandDefsPerId;


    public RunCommandHandler(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;

        final SpoofaxEclipseComponent component = SpoofaxPlugin.getComponent();
        this.pieRunner = component.getPieRunner();

        final HashMap<String, CommandDef> transformDefsPerId = new HashMap<>();
        for(CommandDef commandDef : languageComponent.getLanguageInstance().getCommandDefs()) {
            transformDefsPerId.put(commandDef.getId(), commandDef);
        }
        this.commandDefsPerId = new MapView<>(transformDefsPerId);
    }


    @Override public @Nullable Object execute(ExecutionEvent event) throws ExecutionException {
        final @Nullable String dataStr = event.getParameter(dataParameterId);
        if(dataStr == null) {
            throw new ExecutionException("Cannot execute command, no argument for '" + dataParameterId + "' parameter was set");
        }
        final CommandData data = SerializationUtil.deserialize(dataStr, RunCommandHandler.class.getClassLoader());
        final @Nullable CommandDef def = commandDefsPerId.get(data.commandId);
        if(def == null) {
            throw new ExecutionException("Cannot execute command with ID '" + data.commandId + "', command with that ID was not found in language '" + languageComponent.getLanguageInstance().getDisplayName() + "'");
        }
        final CommandRequest<?> request = data.toCommandRequest(def);
        try {
            try(final MixedSession session = languageComponent.getPieProvider().get().newSession()) {
                pieRunner.requireCommand(languageComponent, request, data.contexts, session, null);
            }
        } catch(ExecException e) {
            throw new ExecutionException("Cannot execute command request '" + request + "', execution failed unexpectedly", e);
        } catch(InterruptedException e) {
            // Ignore
        }
        return null;
    }
}
