package mb.spoofax.eclipse.editor;

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

public class EditorUpdateJob extends Job {
    private final Logger logger;
    private final PieRunner pieRunner;
    private final EclipseLanguageComponent languageComponent;
    private final PieComponent pieComponent;
    private final String languageDisplayName;
    private final @Nullable IProject project;
    private final IFile file;
    private final IDocument document;
    private final SpoofaxEditor editor;

    public EditorUpdateJob(
        LoggerFactory loggerFactory,
        PieRunner pieRunner,
        EclipseLanguageComponent languageComponent,
        PieComponent pieComponent,
        @Nullable IProject project,
        IFile file,
        IDocument document,
        SpoofaxEditor editor
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

    @Override public boolean belongsTo(Object family) {
        return editor.equals(family);
    }

    private IStatus update(IProgressMonitor monitor) throws ExecException, InterruptedException {
        pieRunner.addOrUpdateEditor(languageComponent, pieComponent.getPie(), project, file, document, editor, monitor);
        return StatusUtil.success();
    }
}
