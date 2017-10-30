package mb.spoofax.runtime.eclipse.editor;

import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;

import mb.log.Logger;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.ObsFuncApp;
import mb.pie.runtime.core.PushingExecutor;
import mb.spoofax.runtime.eclipse.pipeline.PipelineAdapter;
import mb.spoofax.runtime.eclipse.pipeline.WorkspaceUpdate;
import mb.spoofax.runtime.eclipse.util.StatusUtils;
import mb.vfs.path.PPath;

public class EditorUpdateJob extends Job {
    private final Logger logger;

    private final PushingExecutor executor;
    private final PipelineAdapter pipelineAdapter;
    private final WorkspaceUpdate update;
    private final SpoofaxEditor editor;

    private final String text;
    private final PPath file;
    private final PPath project;

    private final IEditorInput input;
    private final IResource eclipseFile;


    public EditorUpdateJob(Logger logger, PushingExecutor executor, PipelineAdapter pipelineAdapter,
        WorkspaceUpdate update, SpoofaxEditor editor, String text, PPath file, PPath project, IEditorInput input,
        IResource eclipseFile) {
        super("Editor update");
        this.logger = logger;
        this.executor = executor;
        this.pipelineAdapter = pipelineAdapter;
        this.update = update;
        this.editor = editor;

        this.text = text;
        this.input = input;
        this.file = file;

        this.eclipseFile = eclipseFile;
        this.project = project;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running editor update job for {}", input);
        try {
            return update(monitor);
        } catch(ExecException | CoreException e) {
            final String message = "Editor update for " + input + " failed";
            logger.error(message, e);
            return StatusUtils.error(message, e);
        }
    }

    @Override public boolean belongsTo(Object family) {
        return input.equals(family);
    }

    private IStatus update(IProgressMonitor monitor) throws ExecException, CoreException {
        final ArrayList<ObsFuncApp<Serializable, Serializable>> obsFuncApps = new ArrayList<>();
        obsFuncApps.add(pipelineAdapter.editorObsFuncApp(text, file, project, editor, update));
        executor.require(obsFuncApps, new ArrayList<>());

        update.updateMessagesSync(eclipseFile, monitor);
        update.updateStyleAsync(monitor);

        return StatusUtils.success();
    }
}
