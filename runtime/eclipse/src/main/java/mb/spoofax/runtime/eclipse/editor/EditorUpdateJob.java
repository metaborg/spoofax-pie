package mb.spoofax.runtime.eclipse.editor;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;

import mb.log.Logger;
import mb.pie.runtime.core.BuildException;
import mb.pie.runtime.core.BuildManager;
import mb.spoofax.runtime.eclipse.build.WorkspaceUpdate;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.util.StatusUtils;
import mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution;
import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.style.Styling;
import mb.spoofax.runtime.pie.generated.processString;
import mb.vfs.path.PPath;

public class EditorUpdateJob extends Job {
    private final Logger logger;

    private final BuildManager buildManager;
    private final WorkspaceUpdate update;
    private final SpoofaxEditor editor;

    private final String text;
    private final IEditorInput input;
    private final PPath file;
    private final IResource eclipseFile;
    private final PPath project;
    private final PPath workspaceRoot;


    public EditorUpdateJob(Logger logger, BuildManager buildManager, WorkspaceUpdate update, SpoofaxEditor editor,
        String text, IEditorInput input, PPath file, IResource eclipseFile, PPath project, PPath workspaceRoot) {
        super("Editor update");
        this.logger = logger;
        this.buildManager = buildManager;
        this.update = update;
        this.editor = editor;
        this.text = text;
        this.input = input;
        this.file = file;
        this.eclipseFile = eclipseFile;
        this.project = project;
        this.workspaceRoot = workspaceRoot;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running editor update job for {}", input);
        try {
            return update(monitor);
        } catch(BuildException | CoreException e) {
            final String message = "Editor update for " + input + " failed";
            logger.error(message, e);
            return StatusUtils.error(message, e);
        }
    }

    @Override public boolean belongsTo(Object family) {
        return input.equals(family);
    }

    private IStatus update(IProgressMonitor monitor) throws BuildException, CoreException {
        final processString.Output output =
            buildManager.build(processString.class, new processString.Input(text, file, project, workspaceRoot));

        update.addClear(file);
        if(output != null) {
            final ArrayList<Msg> messages = output.component2();
            update.replaceMessages(file, messages);

            final @Nullable Styling styling = output.component3();
            if(styling != null) {
                update.updateStyle(editor, text, styling);
            } else {
                update.removeStyle(editor, text.length());
            }
            
            final @Nullable ConstraintSolverSolution solution = output.component5();
            if(solution != null) {
                update.addMessages(solution.getFileMessages());
                update.addMessages(solution.getFileUnsolvedMessages());
                update.addMessages(project, solution.getProjectMessages());
                update.addMessages(project, solution.getProjectUnsolvedMessages());
            }
        } else {
            update.removeStyle(editor, text.length());
        }

        update.updateMessagesSync(eclipseFile, monitor);
        update.updateStyleAsync(monitor);

        return StatusUtils.success();
    }
}
