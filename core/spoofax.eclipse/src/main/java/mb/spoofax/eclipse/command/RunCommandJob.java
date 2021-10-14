package mb.spoofax.eclipse.command;

import mb.common.util.ExceptionPrinter;
import mb.common.util.StringBuilderOutputStream;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import mb.spoofax.core.language.command.ResourcePathWithKind;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.pie.CommandContextAndFeedback;
import mb.spoofax.eclipse.pie.PieRunner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import java.io.PrintStream;
import java.util.ArrayList;

public class RunCommandJob extends Job {
    private final Logger logger;
    private final EclipseLanguageComponent languageComponent;
    private final Pie pie;
    private final PieRunner pieRunner;

    private final CommandData data;
    private final CommandRequest<?> request;


    public RunCommandJob(
        LoggerFactory loggerFactory,
        EclipseLanguageComponent languageComponent,
        Pie pie,
        PieRunner pieRunner,
        CommandData data,
        CommandRequest<?> request
    ) {
        super(request.def().getDisplayName());
        this.logger = loggerFactory.create(getClass());
        this.languageComponent = languageComponent;
        this.pie = pie;
        this.pieRunner = pieRunner;
        this.data = data;
        this.request = request;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        final String pluginId = languageComponent.getEclipseIdentifiers().getPlugin();
        logger.trace("Running command '{}' from language '{}'", request.def().getDisplayName(), languageComponent.getLanguageInstance().getDisplayName());
        try(final MixedSession session = pie.newSession()) {
            final ArrayList<CommandContextAndFeedback> contextsAndFeedbacks = pieRunner.requireCommand(request, data.contexts, pie, session, monitor);
            final ArrayList<Throwable> exceptions = new ArrayList<>();
            final StringBuilder errorSB = new StringBuilder();
            final StringBuilder messagesSB = new StringBuilder();
            for(CommandContextAndFeedback contextAndFeedback : contextsAndFeedbacks) {
                final CommandContext context = contextAndFeedback.context;
                final CommandFeedback feedback = contextAndFeedback.feedback;
                if(feedback.hasErrorMessagesOrException()) {
                    final @Nullable String resourceStr = context.getResourcePathWithKind()
                        .map(ResourcePathWithKind::toDisplayString)
                        .orElseGet(() -> context.getResourceKey().map(ResourceKey::toString).orElse(null));
                    errorSB.append("Running command '");
                    errorSB.append(request.def().getDisplayName());
                    if(resourceStr != null) {
                        errorSB.append("' on '");
                        errorSB.append(resourceStr);
                    }
                    errorSB.append("' failed.\n");
                }
                if(feedback.hasErrorMessages()) {
                    final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
                    @Nullable CommandContext enclosingContext = context.getEnclosing(EnclosingCommandContextType.Project);
                    if(enclosingContext == null) {
                        enclosingContext = context.getEnclosing(EnclosingCommandContextType.Directory);
                    }
                    if(enclosingContext != null) {
                        enclosingContext.getResourcePathWithKind().ifPresent(p -> exceptionPrinter.addCurrentDirectoryContext(p.getPath()));
                    }
                    exceptionPrinter.printMessages(feedback.getMessages(), new PrintStream(new StringBuilderOutputStream(messagesSB)));
                }
                final @Nullable Throwable exception = feedback.getException();
                if(exception != null) {
                    exceptions.add(exception);
                }
            }

            if(errorSB.length() == 0) {
                return new Status(IStatus.OK, pluginId, errorSB.toString());
            } else {
                final MultiStatus multiStatus = new MultiStatus(pluginId, IStatus.ERROR, errorSB.toString(), null);
                if(messagesSB.length() != 0) {
                    multiStatus.add(new Status(IStatus.ERROR, pluginId, messagesSB.toString()));
                }
                for(Throwable e : exceptions) {
                    multiStatus.add(exceptionToStatus(e, pluginId));
                }
                return multiStatus;
            }
        } catch(ExecException | RuntimeException e) {
            return exceptionToStatus(e, pluginId);
        } catch(InterruptedException e) {
            // Execution was interrupted. No need to re-set interrupt, as we are the final handler of the interrupt.
            return new Status(IStatus.CANCEL, pluginId, "");
        }
    }

    @Override protected void canceling() {
        final Thread thread = getThread();
        if(thread == null) {
            return;
        }
        thread.interrupt();
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
