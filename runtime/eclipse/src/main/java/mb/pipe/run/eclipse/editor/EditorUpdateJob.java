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
    private final IResource eclipseResource;


    public EditorUpdateJob(Logger logger, BuildManager buildManager, Updater updater, ISourceViewer sourceViewer,
        String text, Context context, IEditorInput input, IResource eclipseResource) {
        super("Editor update");
        this.logger = logger;
        this.buildManager = buildManager;
        this.updater = updater;
        this.sourceViewer = sourceViewer;
        this.text = text;
        this.context = context;
        this.input = input;
        this.eclipseResource = eclipseResource;
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
            buildManager.build(processString.class, new processString.Input(text, context));

        final List<Msg> messages = output.component4();
        updater.updateMessagesSync(eclipseResource, messages, monitor);

        final @Nullable Styling styling = output.component5();
        if(styling != null) {
            updater.updateStyle(sourceViewer, styling, monitor);
        }

        return StatusUtils.success();
    }
}
