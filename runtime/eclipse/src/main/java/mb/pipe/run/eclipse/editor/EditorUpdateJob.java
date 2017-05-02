package mb.pipe.run.eclipse.editor;

import java.util.Collection;

import javax.annotation.Nullable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;

import build.pluto.builder.BuildManager;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.database.XodusDatabase;
import build.pluto.util.LogReporting;
import mb.pipe.run.core.log.ILogger;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.core.model.message.IMsg;
import mb.pipe.run.core.model.style.IStyling;
import mb.pipe.run.eclipse.build.Updater;
import mb.pipe.run.eclipse.util.StatusUtils;
import mb.pipe.run.pluto.generated.processString;

public class EditorUpdateJob extends Job {
    private final ILogger logger;

    private final Updater updater;
    private final ISourceViewer sourceViewer;

    private final String text;
    private final IContext context;
    private final IEditorInput input;
    private final IResource eclipseResource;


    public EditorUpdateJob(ILogger logger, Updater updater, ISourceViewer sourceViewer, String text, IContext context,
        IEditorInput input, IResource eclipseResource) {
        super("Editor update");
        this.logger = logger;
        this.updater = updater;
        this.sourceViewer = sourceViewer;
        this.text = text;
        this.context = context;
        this.input = input;
        this.eclipseResource = eclipseResource;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running editor update job for {}", input);

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();

        try {
            final IStatus status = update(workspace, monitor);
            return status;
        } catch(Throwable e) {
            return StatusUtils.error(e);
        }
    }

    @Override public boolean belongsTo(Object family) {
        return input.equals(family);
    }

    private IStatus update(IWorkspace workspace, final IProgressMonitor monitor) throws Throwable {
        final BuildRequest<?, processString.Output, ?, ?> buildRequest =
            processString.request(new processString.Input(context, null, text, context));

        try(final BuildManager buildManager =
            new BuildManager(new LogReporting(), XodusDatabase.createFileDatabase("pipeline-experiment"))) {
            final processString.Output output = buildManager.requireInitially(buildRequest).getBuildResult();

            final Collection<IMsg> messages = (Collection<IMsg>) output.getPipeVal().get(3);
            updater.updateMessagesSync(eclipseResource, messages, monitor);

            final @Nullable IStyling styling = (IStyling) output.getPipeVal().get(4);
            updater.updateStyle(sourceViewer, styling, monitor);
        }

        return StatusUtils.success();
    }
}
