package mb.spoofax.eclipse.editor;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.dagger.PieComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.StatusUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;

public class EditorUpdateJob extends Job {
    @AssistedFactory public interface Factory {
        EditorUpdateJob create(
            EclipseLanguageComponent languageComponent,
            PieComponent pieComponent,
            @Nullable IProject project,
            IFile file,
            IDocument document,
            IEditorInput input,
            SpoofaxEditorBase editor
        );
    }

    private final Logger logger;
    private final PieRunner pieRunner;
    private final EclipseLanguageComponent languageComponent;
    private final PieComponent pieComponent;
    private final String languageDisplayName;
    private final @Nullable IProject project;
    private final IFile file;
    private final IDocument document;
    private final IEditorInput input;
    private final SpoofaxEditorBase editor;

    @AssistedInject public EditorUpdateJob(
        LoggerFactory loggerFactory,
        PieRunner pieRunner,
        @Assisted EclipseLanguageComponent languageComponent,
        @Assisted PieComponent pieComponent,
        @Assisted @Nullable IProject project,
        @Assisted IFile file,
        @Assisted IDocument document,
        @Assisted IEditorInput input,
        @Assisted SpoofaxEditorBase editor
    ) {
        super(languageComponent.getLanguageInstance().getDisplayName() + " editor update");
        this.logger = loggerFactory.create(getClass());
        this.pieRunner = pieRunner;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
        this.languageDisplayName = languageComponent.getLanguageInstance().getDisplayName();
        this.project = project;
        this.file = file;
        this.document = document;
        this.input = input;
        this.editor = editor;
    }

    @Override protected IStatus run(@NonNull IProgressMonitor monitor) {
        logger.trace("Running {} editor update job for {}", languageDisplayName, file);
        try {
            return update(monitor);
        } catch(@SuppressWarnings("unused") InterruptedException e) {
            return StatusUtil.cancel();
        } catch(ExecException e) {
            final String message = languageDisplayName + " editor update for " + file + " failed";
            logger.error(message, e);
            return StatusUtil.error(message, e);
        }
    }

    @Override protected void canceling() {
        final Thread thread = getThread();
        if(thread == null) {
            return;
        }
        thread.interrupt();
    }

    @Override public boolean belongsTo(Object family) {
        return input.equals(family) || editor.equals(family);
    }

    private IStatus update(IProgressMonitor monitor) throws ExecException, InterruptedException {
        pieRunner.addOrUpdateEditor(languageComponent, pieComponent.getPie(), project, file, document, editor, monitor);
        return StatusUtil.success();
    }
}
