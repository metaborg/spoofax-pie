package mb.spoofax.runtime.eclipse.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;

import mb.log.Logger;
import mb.pie.runtime.core.ExecException;
import mb.spoofax.runtime.eclipse.pipeline.PipelineAdapter;
import mb.spoofax.runtime.eclipse.util.StatusUtils;
import mb.vfs.path.PPath;

public class EditorUpdateJob extends Job {
    private final Logger logger;
    private final PipelineAdapter pipelineAdapter;
    private final SpoofaxEditor editor;
    private final String text;
    private final PPath file;
    private final PPath project;
    private final IEditorInput input;


    public EditorUpdateJob(Logger logger, PipelineAdapter pipelineAdapter, SpoofaxEditor editor, String text,
        PPath file, PPath project, IEditorInput input) {
        super("Editor update");
        this.logger = logger;
        this.pipelineAdapter = pipelineAdapter;
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
            return StatusUtils.cancel();
        } catch(ExecException e) {
            final String message = "Editor update for " + input + " failed";
            logger.error(message, e);
            return StatusUtils.error(message, e);
        }
    }

    @Override public boolean belongsTo(Object family) {
        return input.equals(family);
    }

    private IStatus update(IProgressMonitor monitor) throws ExecException, InterruptedException {
        pipelineAdapter.updateEditor(editor, text, file, project, monitor);
        return StatusUtils.success();
    }
}
