package mb.spoofax.eclipse.pie;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.Pie;
import mb.pie.api.PieSession;
import mb.pie.api.Task;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.NullCancelled;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.resource.EclipseResourceRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import javax.inject.Inject;
import javax.inject.Provider;

public class PieRunner {
    private final Logger logger;
    private final Pie pie;
    private final EclipseResourceRegistry eclipseResourceRegistry;
    private final Provider<WorkspaceUpdate> workspaceUpdateProvider;


    @Inject
    public PieRunner(
        LoggerFactory loggerFactory,
        Pie pie,
        EclipseResourceRegistry eclipseResourceRegistry,
        Provider<WorkspaceUpdate> workspaceUpdateProvider
    ) {
        this.logger = loggerFactory.create(getClass());
        this.pie = pie;
        this.eclipseResourceRegistry = eclipseResourceRegistry;
        this.workspaceUpdateProvider = workspaceUpdateProvider;
    }


    public <D extends IDocument & IDocumentExtension4> void addOrUpdateEditor(
        LanguageComponent languageComponent,
        IFile file,
        D document,
        SpoofaxEditor editor,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        logger.trace("Adding or updating editor for file '{}'", file);

        final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
        eclipseResourceRegistry.addDocumentOverride(resourceKey, document, file);
        final WorkspaceUpdate workspaceUpdate = workspaceUpdateProvider.get();

        try(final PieSession session = languageComponent.newPieSession()) {
            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();

            final Task<@Nullable Styling> styleTask = languageInstance.createStyleTask(resourceKey);
            final String text = document.get();
            logger.trace("Require top-down '{}'", styleTask);
            final @Nullable Styling styling = session.requireTopDown(styleTask, monitorCancelled(monitor));
            if(styling != null) {
                workspaceUpdate.updateStyle(editor, text, styling);
            } else {
                workspaceUpdate.removeStyle(editor, text.length());

            }
            logger.trace("Unobserve '{}'", styleTask);
            session.setUnobserved(styleTask); // OPTO: observing and then immediately unobserving is not efficient.

            final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourceKey);
            logger.trace("Require top-down '{}'", checkTask);
            final KeyedMessages messages = session.requireTopDown(checkTask, monitorCancelled(monitor));
            workspaceUpdate.clearMessages(file);
            workspaceUpdate.replaceMessages(messages);
        }

        workspaceUpdate.update(file, monitor);
    }

    public void removeEditor(
        LanguageComponent languageComponent,
        IFile file
    ) {
        logger.trace("Removing editor for file '{}'", file);

        final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
        eclipseResourceRegistry.removeDocumentOverride(resourceKey);

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();

        pie.removeCallback(languageInstance.createStyleTask(resourceKey));
        final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourceKey);
        pie.removeCallback(checkTask);
        try(final PieSession session = languageComponent.newPieSession()) {
            logger.trace("Unobserve '{}'", checkTask);
            session.setUnobserved(checkTask);
        }
    }


    public void filesChanged() {

    }


    private static Cancelled monitorCancelled(@Nullable IProgressMonitor monitor) {
        if(monitor != null) {
            return new MonitorCancelToken(monitor);
        } else {
            return new NullCancelled();
        }
    }
}
