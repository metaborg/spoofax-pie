package mb.spoofax.eclipse.command;

import mb.common.util.MapView;
import mb.common.util.SerializationUtil;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.CommandContextAndFeedback;
import mb.spoofax.eclipse.pie.PieRunner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import java.util.ArrayList;
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
        try(final MixedSession session = languageComponent.getPie().newSession()) {
            final ArrayList<CommandContextAndFeedback> contextsAndFeedbacks = pieRunner.requireCommand(languageComponent, request, data.contexts, session, null);
            final ArrayList<Exception> exceptions = new ArrayList<>();
            final StringBuilder sb = new StringBuilder();
            boolean error = false;
            for(CommandContextAndFeedback contextAndFeedback : contextsAndFeedbacks) {
                final CommandContext context = contextAndFeedback.context;
                final CommandFeedback feedback = contextAndFeedback.feedback;
                if(feedback.hasErrorMessagesOrException()) {
                    error = true;
                    sb.append("Executing command request '").append(request.def().getDisplayName()).append("' on '").append(context).append("' failed.\n");
                }
                if(feedback.hasErrorMessages()) {
                    sb.append("The following messages were produced:\n");
                    feedback.getMessages().addToStringBuilder(sb);
                }
                final @Nullable Exception exception = feedback.getException();
                if(exception != null) {
                    exceptions.add(exception);
                }
            }
            final String pluginId = languageComponent.getEclipseIdentifiers().getPlugin();
            final MultiStatus multiStatus = new MultiStatus(pluginId, IStatus.ERROR, "Exceptions occurred", null);
            for(Exception e : exceptions) {
                multiStatus.add(new Status(IStatus.ERROR, pluginId, e.getMessage(), e));
            }
            if(error) {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().asyncExec(() -> {
                    final Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    ErrorDialog.openError(activeShell, "Executing command request '" + request.def().getDisplayName() + "' failed unexpectedly", sb.toString(), multiStatus);
                });
            }
        } catch(ExecException e) {
            throw new ExecutionException("Executing command request '" + request.def().getDisplayName() + "' failed unexpectedly", e);
        } catch(InterruptedException e) {
            // Execution was interrupted. No need to re-set interrupt, as we are the final handler of the interrupt.
        }
        return null;
    }
}
