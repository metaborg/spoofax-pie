package mb.spoofax.eclipse.pie;

import mb.common.message.MessageCollection;
import mb.common.style.Styling;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.Pie;
import mb.pie.api.PieSession;
import mb.pie.api.Task;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.NullCancelled;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseResourceKey;
import mb.spoofax.eclipse.util.StyleUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.TextPresentation;

import javax.inject.Inject;
import java.util.HashSet;

public class PieRunner {
    private final Logger logger;
    private final Pie pie;
    private final StyleUtil styleUtil;


    @Inject public PieRunner(LoggerFactory loggerFactory, Pie pie, StyleUtil styleUtil) {
        this.logger = loggerFactory.create(getClass());
        this.pie = pie;
        this.styleUtil = styleUtil;
    }


    public void addOrUpdateEditor(
        LanguageComponent languageComponent,
        IFile file,
        String text,
        SpoofaxEditor editor,
        @Nullable IProgressMonitor monitor
    ) {
        try(final PieSession session = languageComponent.newPieSession()) {
            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
            final Cancelled cancelled = monitorCancelled(monitor);
            final EclipseResourceKey resourceKey = new EclipseResourceKey(file);

            // Set observer for styling task, and execute it if it has not been executed yet.
            final Task<Styling> stylingTask = languageInstance.createStylingTask(resourceKey);
            pie.setObserver(stylingTask, (styling) -> {
                final TextPresentation textPresentation = styleUtil.createTextPresentation(styling, text.length());
                editor.setStyleAsync(textPresentation, text, monitor);
            });
            if(!pie.hasBeenExecuted(stylingTask)) {
                session.requireTopDown(stylingTask, cancelled);
            }

            // Set observer for messages task, and execute it if it has not been executed yet.
            final Task<MessageCollection> messagesTask = languageInstance.createMessagesTask(resourceKey);
            pie.setObserver(messagesTask, (messages) -> {
                // TODO: process messages
            });
            if(!pie.hasBeenExecuted(messagesTask)) {
                session.requireTopDown(messagesTask, cancelled);
            }

            // Execute bottom-up build for changed file.
            final HashSet<ResourceKey> changedResources = new HashSet<>();
            changedResources.add(new EclipseResourceKey(file));
            session.requireBottomUp(changedResources);
        } catch(InterruptedException e) {
            logger.trace("Bottom-up build for adding or updating editor of file '{}' has been interrupted", e, file);
        } catch(ExecException e) {
            logger.trace("Bottom-up build for adding or updating editor of file '{}' failed unexpectedly", e, file);
        }
    }

    public void removeEditor(
        LanguageComponent languageComponent,
        IFile file
    ) {
        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final EclipseResourceKey resourceKey = new EclipseResourceKey(file);
        pie.removeObserver(languageInstance.createStylingTask(resourceKey));
        pie.removeObserver(languageInstance.createMessagesTask(resourceKey));
    }


    private static Cancelled monitorCancelled(@Nullable IProgressMonitor monitor) {
        if(monitor != null) {
            return new MonitorCancelToken(monitor);
        } else {
            return new NullCancelled();
        }
    }
}
