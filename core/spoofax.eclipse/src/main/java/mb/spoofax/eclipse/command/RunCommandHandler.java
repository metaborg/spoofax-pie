package mb.spoofax.eclipse.command;

import mb.common.util.MapView;
import mb.common.util.SerializationUtil;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.ResourcePathWithKind;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.CommandContextAndFeedback;
import mb.spoofax.eclipse.pie.PieRunner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class RunCommandHandler extends AbstractHandler {
    public final static String dataParameterId = "data";

    private static final Bundle bundle = FrameworkUtil.getBundle(RunCommandHandler.class);
    private static final ILog logger = Platform.getLog(bundle);

    private final EclipseLanguageComponent languageComponent;
    private final PieComponent pieComponent;

    private final PieRunner pieRunner;

    private final MapView<String, CommandDef<?>> commandDefsPerId;


    public RunCommandHandler(EclipseLanguageComponent languageComponent, PieComponent pieComponent) {
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;

        final EclipsePlatformComponent component = SpoofaxPlugin.getPlatformComponent();
        this.pieRunner = component.getPieRunner();

        final HashMap<String, CommandDef<?>> transformDefsPerId = new HashMap<>();
        for(CommandDef<?> commandDef : languageComponent.getLanguageInstance().getCommandDefs()) {
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
        final @Nullable CommandDef<?> def = commandDefsPerId.get(data.commandId);
        if(def == null) {
            throw new ExecutionException("Cannot execute command with ID '" + data.commandId + "', command with that ID was not found in language '" + languageComponent.getLanguageInstance().getDisplayName() + "'");
        }
        final CommandRequest<?> request = data.toCommandRequest(def);
        final String pluginId = languageComponent.getEclipseIdentifiers().getPlugin();
        final Pie pie = pieComponent.getPie();
        // TODO: run this in a Job, both to enable progress/cancellation, and better error reporting.
        try(final MixedSession session = pie.newSession()) {
            final ArrayList<CommandContextAndFeedback> contextsAndFeedbacks = pieRunner.requireCommand(request, data.contexts, pie, session, null);
            final ArrayList<Exception> exceptions = new ArrayList<>();
            final StringBuilder sb = new StringBuilder();
            boolean error = false;
            for(CommandContextAndFeedback contextAndFeedback : contextsAndFeedbacks) {
                final CommandContext context = contextAndFeedback.context;
                final CommandFeedback feedback = contextAndFeedback.feedback;
                if(feedback.hasErrorMessagesOrException()) {
                    error = true;
                    final @Nullable String resourceStr = context.getResourcePathWithKind()
                        .map(ResourcePathWithKind::toString)
                        .orElseGet(() -> context.getResourceKey().map(ResourceKey::toString).orElse(null));
                    sb.append("Executing command request '");
                    sb.append(request.def().getDisplayName());
                    if(resourceStr != null) {
                        sb.append("' on '");
                        sb.append(resourceStr);
                    }
                    sb.append("' failed.\n");
                }
                if(feedback.hasErrorMessages()) {
                    sb.append("\nThe following messages were produced:\n");
                    feedback.getMessages().addToStringBuilder(sb);
                }
                final @Nullable Exception exception = feedback.getException();
                if(exception != null) {
                    exceptions.add(exception);
                }
            }
            final IStatus status;
            if(exceptions.isEmpty()) {
                status = new Status(IStatus.ERROR, pluginId, sb.toString());
            } else {
                final MultiStatus multiStatus = new MultiStatus(pluginId, IStatus.ERROR, sb.toString(), null);
                for(Exception e : exceptions) {
                    multiStatus.add(exceptionToStatus(e, pluginId));
                }
                status = multiStatus;
            }
            if(error) {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().asyncExec(() -> {
                    final Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    ErrorDialog.openError(activeShell, "Executing command '" + request.def().getDisplayName() + "' failed", null, status);
                });
                logger.log(status);
            }
        } catch(ExecException | RuntimeException e) {
            final IStatus status = new Status(IStatus.ERROR, pluginId, "Executing command '" + request.def().getDisplayName() + "' failed unexpectedly", e);
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().asyncExec(() -> {
                final Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                ErrorDialog.openError(activeShell, "Executing command '" + request.def().getDisplayName() + "' failed", null, status);
            });
            logger.log(status);
        } catch(InterruptedException e) {
            // Execution was interrupted. No need to re-set interrupt, as we are the final handler of the interrupt.
        }
        return null;
    }

    private IStatus exceptionToStatus(Throwable e, String pluginId) {
        final @Nullable Throwable cause = e.getCause();
        if(cause == null) {
            return new Status(IStatus.ERROR, pluginId, e.getMessage(), e);
        } else {
            return new MultiStatus(pluginId, IStatus.ERROR, new IStatus[]{exceptionToStatus(cause, pluginId)}, e.getMessage(), e);
        }
    }
}
