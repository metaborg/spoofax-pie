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

public class EditorCloseJob extends Job {
    @AssistedFactory public interface Factory {
        EditorCloseJob create(
            EclipseLanguageComponent languageComponent,
            PieComponent pieComponent,
            @Nullable IProject project,
            IFile file
        );
    }

    private final Logger logger;
    private final PieRunner pieRunner;
    private final EclipseLanguageComponent languageComponent;
    private final PieComponent pieComponent;
    private final String languageDisplayName;
    private final @Nullable IProject project;
    private final IFile file;

    @AssistedInject public EditorCloseJob(
        LoggerFactory loggerFactory,
        PieRunner pieRunner,
        @Assisted EclipseLanguageComponent languageComponent,
        @Assisted PieComponent pieComponent,
        @Assisted @Nullable IProject project,
        @Assisted IFile file
    ) {
        super(languageComponent.getLanguageInstance().getDisplayName() + " editor close");
        this.logger = loggerFactory.create(getClass());
        this.pieRunner = pieRunner;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
        this.languageDisplayName = languageComponent.getLanguageInstance().getDisplayName();
        this.project = project;
        this.file = file;
    }

    @Override protected IStatus run(@NonNull IProgressMonitor monitor) {
        logger.trace("Running {} editor close job for {}", languageDisplayName, file);
        try {
            return update();
        } catch(@SuppressWarnings("unused") InterruptedException e) {
            return StatusUtil.cancel();
        } catch(ExecException e) {
            final String message = languageDisplayName + " editor close for " + file + " failed";
            logger.error(message, e);
            return StatusUtil.error(message, e);
        }
    }

    private IStatus update() throws ExecException, InterruptedException {
        pieRunner.removeEditor(languageComponent, pieComponent, project, file);
        return StatusUtil.success();
    }
}
