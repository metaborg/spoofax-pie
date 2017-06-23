package mb.pipe.run.eclipse.editor;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;

import mb.ceres.BuildException;
import mb.ceres.BuildManager;
import mb.pipe.run.ceres.generated.processString;
import mb.pipe.run.core.log.Logger;
import mb.pipe.run.core.model.Context;
import mb.pipe.run.core.model.message.Msg;
import mb.pipe.run.core.model.style.Styling;
import mb.pipe.run.core.path.PPath;
import mb.pipe.run.eclipse.build.Updater;
import mb.pipe.run.eclipse.util.Nullable;
import mb.pipe.run.eclipse.util.StatusUtils;

public class EditorUpdateJob extends Job {
    private final Logger logger;

    private final BuildManager buildManager;
    private final Updater updater;
    private final ISourceViewer sourceViewer;

    private final String text;
    private final Context context;
    private final IEditorInput input;
    private final PPath file;
    private final IResource eclipseFile;
    private final PPath workspaceRoot;


    public EditorUpdateJob(Logger logger, BuildManager buildManager, Updater updater, ISourceViewer sourceViewer,
        String text, Context context, IEditorInput input, PPath file, IResource eclipseFile, PPath workspaceRoot) {
        super("Editor update");
        this.logger = logger;
        this.buildManager = buildManager;
        this.updater = updater;
        this.sourceViewer = sourceViewer;
        this.text = text;
        this.context = context;
        this.input = input;
        this.file = file;
        this.eclipseFile = eclipseFile;
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
            buildManager.build(processString.class, new processString.Input(text, file, context, workspaceRoot));

        if(output != null) {
            final List<Msg> messages = output.component2();
            updater.updateMessagesSync(eclipseFile, messages, monitor);

            final @Nullable Styling styling = output.component3();
            if(styling != null) {
                updater.updateStyleAsync(sourceViewer, styling, monitor);
            } else {
                updater.removeStyleAsync(sourceViewer, text.length(), monitor);
            }
        } else {
            updater.clearMessagesSync(eclipseFile, monitor);
            updater.removeStyleAsync(sourceViewer, text.length(), monitor);
        }

        return StatusUtils.success();
    }
}
