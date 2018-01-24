package mb.spoofax.runtime.eclipse.pipeline;

import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mb.log.Logger;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.FuncApp;
import mb.pie.runtime.core.exec.ObservingExecutor;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.pie.PieSrv;
import mb.spoofax.runtime.pie.builder.SpoofaxPipeline;
import mb.spoofax.runtime.pie.generated.processEditor;
import mb.spoofax.runtime.pie.generated.processProject;
import mb.util.async.Cancelled;
import mb.vfs.path.PPath;

public class ObservingPipelineAdapter implements PipelineAdapter {
    private final PipelineObservers observers;
    private final PipelinePathChanges pathChanges;
    private final SpoofaxPipeline pipeline;

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final WorkspaceUpdateFactory workspaceUpdateFactory;

    private final IWorkspaceRoot eclipseRoot;
    private final PPath root;
    private final ObservingExecutor executor;


    @Inject public ObservingPipelineAdapter(PipelineObservers observers, PipelinePathChanges pathChanges, Logger logger,
        EclipsePathSrv pathSrv, PieSrv pieSrv, WorkspaceUpdateFactory workspaceUpdateFactory) {
        this.observers = observers;
        this.pathChanges = pathChanges;
        this.pipeline = SpoofaxPipeline.INSTANCE;

        this.logger = logger.forContext(getClass());
        this.pathSrv = pathSrv;
        this.workspaceUpdateFactory = workspaceUpdateFactory;

        this.eclipseRoot = ResourcesPlugin.getWorkspace().getRoot();
        this.root = pathSrv.resolve(eclipseRoot);

        this.executor = pieSrv.getObservingExecutor(root, SpoofaxPlugin.useInMemoryStore);
    }


    @Override public void addProject(IProject project) {
        final PPath mbProject = pathSrv.resolve(project);
        if(mbProject == null) {
            logger.error("Failed to set pipeline observer; cannot resolve Eclipse project {} to a path", project);
            return;
        }

        logger.debug("Setting pipeline observer for project {}", project);
        executor.setObserver(project, projectApp(mbProject), projectObs(mbProject));
    }

    @Override public void buildProject(IProject project, int buildKind, @Nullable IResourceDelta delta,
        @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException, CoreException {
        final PPath mbProject = pathSrv.resolve(project);
        if(mbProject == null) {
            logger.error("Failed to build project; cannot resolve Eclipse project {} to a path", project);
            return;
        }

        final FuncApp<processProject.Input, processProject.Output> app = projectApp(mbProject);
        final Cancelled cancelled = PipelineCancel.cancelled(monitor);

        if(!executor.hasBeenRequired(app) || buildKind == IncrementalProjectBuilder.FULL_BUILD || delta == null) {
            logger.debug("Building project {} from scratch...", project);
            executor.requireTopDown(app, cancelled);
            logger.debug("Done building project {} from scratch", project);
        } else {
            logger.debug("Building project {} incrementally...", project);
            final ArrayList<PPath> changedPaths = pathChanges.changedPaths(delta);
            executor.requireBottomUp(changedPaths, cancelled);
            logger.debug("Done building project {} incrementally", project);
        }
    }

    @Override public void removeProject(IProject eclipseProject) {
        logger.debug("Removing pipeline observer for project {}", eclipseProject);
        executor.removeObserver(eclipseProject);
    }


    @Override public void addEditor(SpoofaxEditor editor, String text, PPath file, PPath project) {
        logger.debug("Setting pipeline observer for editor {}", editor);
        executor.setObserver(editor, editorApp(text, file, project), editorObs(editor, text, file, project));
    }

    @Override public void updateEditor(SpoofaxEditor editor, String text, PPath file, PPath project,
        @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.debug("Setting pipeline observer for editor {}", editor);
        executor.setObserver(editor, editorApp(text, file, project), editorObs(editor, text, file, project));

        try {
            logger.debug("Executing pipeline function for editor {}...", editor);
            executor.requireTopDown(editorApp(text, file, project), PipelineCancel.cancelled(monitor));
        } finally {
            logger.debug("Done executing pipeline function for editor {}", editor);
        }
    }

    @Override public void removeEditor(SpoofaxEditor editor) {
        logger.debug("Removing pipeline observer for editor {}", editor);
        executor.removeObserver(editor);
    }


    @Override public void cleanAll(@Nullable IProgressMonitor monitor) throws CoreException {
        logger.debug("Cleaning all stored pipeline data");
        executor.dropStore();
        executor.dropCache();
        final WorkspaceUpdate update = workspaceUpdateFactory.create();
        update.addClearRec(root);
        update.updateMessagesSync(eclipseRoot, monitor);
    }


    private FuncApp<processProject.Input, processProject.Output> projectApp(PPath project) {
        return pipeline.project(project, root);
    }

    @SuppressWarnings("unchecked") private Function1<Serializable, Unit> projectObs(PPath project) {
        return (Function1<Serializable, Unit>) observers.project(project);
    }

    private FuncApp<processEditor.Input, processEditor.Output> editorApp(String text, PPath file, PPath project) {
        return pipeline.editor(text, file, project, root);
    }

    @SuppressWarnings("unchecked") private Function1<Serializable, Unit> editorObs(SpoofaxEditor editor, String text,
        PPath file, PPath project) {
        return (Function1<Serializable, Unit>) observers.editor(editor, text, file, project);
    }
}
