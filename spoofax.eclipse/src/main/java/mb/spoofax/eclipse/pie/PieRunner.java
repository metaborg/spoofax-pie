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
import mb.pie.runtime.exec.Stats;
import mb.resource.ResourceKey;
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
import java.util.Set;

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
            Stats.reset();
            final @Nullable Styling styling = session.requireWithoutObserving(styleTask, monitorCancelled(monitor));
            logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
            if(styling != null) {
                workspaceUpdate.updateStyle(editor, text, styling);
            } else {
                workspaceUpdate.removeStyle(editor, text.length());
            }

            final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourceKey);
            logger.trace("Require top-down '{}'", checkTask);
            Stats.reset();
            final KeyedMessages messages = session.requireWithoutObserving(checkTask, monitorCancelled(monitor));
            logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
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
            session.unobserve(checkTask);
        }
    }


    public void incrementalBuild(
        LanguageComponent languageComponent,
        Set<ResourceKey> changedResources,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        logger.trace("Running build");

        try(final PieSession session = languageComponent.newPieSession()) {
            logger.trace("Require bottom-up '{}'", changedResources);
            Stats.reset();
            session.updateAffectedBy(changedResources, monitorCancelled(monitor));
            logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
        }
    }


    public boolean isCheckObserved(
        LanguageInstance languageInstance,
        IFile file
    ) {
        final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
        final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourceKey);
        return pie.isObserved(checkTask);
    }

    public void observeCheckTasks(
        LanguageComponent languageComponent,
        Iterable<IFile> files,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        final WorkspaceUpdate workspaceUpdate = workspaceUpdateProvider.get();
        try(final PieSession session = languageComponent.newPieSession()) {
            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
            for(IFile file : files) {
                final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
                final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourceKey);
                if(!pie.isObserved(checkTask)) {
                    logger.trace("Require top-down '{}'", checkTask);
                    Stats.reset();
                    final KeyedMessages messages = session.require(checkTask, monitorCancelled(monitor));
                    logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
                    workspaceUpdate.clearMessages(file);
                    workspaceUpdate.replaceMessages(messages);
                }
            }
        }
        workspaceUpdate.update(null, monitor);
    }

    public void unobserveCheckTasks(
        LanguageComponent languageComponent,
        Iterable<IFile> files,
        @Nullable IProgressMonitor monitor
    ) {
        final WorkspaceUpdate workspaceUpdate = workspaceUpdateProvider.get();
        try(final PieSession session = languageComponent.newPieSession()) {
            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
            for(IFile file : files) {
                final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
                final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourceKey);
                if(pie.isObserved(checkTask)) {
                    session.unobserve(checkTask);
                    workspaceUpdate.clearMessages(file);
                }
            }
        }
        workspaceUpdate.update(null, monitor);
    }


    private static Cancelled monitorCancelled(@Nullable IProgressMonitor monitor) {
        if(monitor != null) {
            return new MonitorCancelToken(monitor);
        } else {
            return new NullCancelled();
        }
    }
}
