package mb.spoofax.eclipse.editor;

import mb.log.api.Logger;
import mb.pie.api.ExecException;
import mb.spoofax.eclipse.util.StatusUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;

public class EditorUpdateJob extends Job {
    private final Logger logger;
    private final SpoofaxEditor editor;
    private final String text;
    private final IFile file;
    private final IProject project;
    private final IEditorInput input;

    public EditorUpdateJob(Logger logger, SpoofaxEditor editor, String text,
        IFile file, IProject project, IEditorInput input) {
        super("Editor update");
        this.logger = logger;
        this.editor = editor;
        this.text = text;
        this.file = file;
        this.project = project;
        this.input = input;
    }

    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running editor update job for {}", input);
        try {
            return update(monitor);
        } catch(@SuppressWarnings("unused") InterruptedException e) {
            return StatusUtil.cancel();
        } catch(ExecException e) {
            final String message = "Editor update for " + input + " failed";
            logger.error(message, e);
            return StatusUtil.error(message, e);
        }
    }

    @Override public boolean belongsTo(Object family) {
        return input.equals(family);
    }

    private IStatus update(IProgressMonitor monitor) throws ExecException, InterruptedException {
//        pipelineAdapter.updateEditor(editor, text, file, project, monitor);
        return StatusUtil.success();
    }
}
