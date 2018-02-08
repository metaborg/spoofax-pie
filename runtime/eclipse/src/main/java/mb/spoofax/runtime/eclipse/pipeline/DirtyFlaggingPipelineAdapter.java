package mb.spoofax.runtime.eclipse.pipeline;

import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mb.log.Logger;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.FuncApp;
import mb.pie.runtime.core.exec.DirtyFlaggingTopDownExecutor;
import mb.pie.runtime.core.exec.ObsFuncApp;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.pie.PieSrv;
import mb.spoofax.runtime.pie.builder.SpoofaxPipeline;
import mb.spoofax.runtime.pie.generated.processEditor;
import mb.spoofax.runtime.pie.generated.processProject;
import mb.vfs.path.PPath;

public class DirtyFlaggingPipelineAdapter implements PipelineAdapter {
    private final PipelineObservers observers;
    private final PipelinePathChanges pathChanges;
    private final SpoofaxPipeline pipeline;

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final WorkspaceUpdateFactory workspaceUpdateFactory;

    private final IWorkspaceRoot eclipseRoot;
    private final PPath root;
    private final DirtyFlaggingTopDownExecutor executor;


    @Inject public DirtyFlaggingPipelineAdapter(PipelineObservers observers, PipelinePathChanges pathChanges,
        Logger logger, EclipsePathSrv pathSrv, PieSrv pieSrv, WorkspaceUpdateFactory workspaceUpdateFactory) {
        this.observers = observers;
        this.pathChanges = pathChanges;
        this.pipeline = SpoofaxPipeline.INSTANCE;

        this.logger = logger.forContext(getClass());
        this.pathSrv = pathSrv;
        this.workspaceUpdateFactory = workspaceUpdateFactory;

        this.eclipseRoot = ResourcesPlugin.getWorkspace().getRoot();
        this.root = pathSrv.resolve(eclipseRoot);

        this.executor = pieSrv.getDirtyFlaggingTopDownExecutor(root, SpoofaxPlugin.useInMemoryStore);
    }


    @Override public void addProject(IProject project) {
        final PPath mbProject = pathSrv.resolve(project);
        if(mbProject == null) {
            logger.error("Failed to add pipeline function; cannot resolve Eclipse project {} to a path", project);
            return;
        }
        logger.debug("Registering pipeline function for project {}", project);
        executor.add(project, projectObsFuncApp(mbProject));
    }

    @Override public void buildProject(IProject project, int buildKind, @Nullable IResourceDelta delta,
        @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException, CoreException {
        logger.debug("Processing resource delta");
        final ArrayList<PPath> changedPaths = pathChanges.changedPaths(delta);
        executor.pathsChanged(changedPaths);
        logger.debug("Dirty flagging...");
        executor.dirtyFlag();
        logger.debug("Done dirty flagging");
        try {
            logger.debug("Executing all pipeline functions...");
            executor.executeAll(PipelineCancel.cancelled(monitor));
        } finally {
            logger.debug("Done executing all pipeline functions");
        }
    }

    @Override public void removeProject(IProject eclipseProject) {
        logger.debug("Unregistering pipeline function for project {}", eclipseProject);
        executor.remove(eclipseProject);
    }


    @Override public void addEditor(SpoofaxEditor editor, String text, IFile file, IProject project) {
        final PPath mbFile = pathSrv.resolve(file);
        if(mbFile == null) {
            logger.error("Failed to set editor pipeline observer; cannot resolve Eclipse file {} to a path", file);
            return;
        }
        final PPath mbProject = pathSrv.resolve(project);
        if(mbProject == null) {
            logger.error("Failed to set editor pipeline observer; cannot resolve Eclipse project {} to a path",
                project);
            return;
        }

        logger.debug("Registering pipeline function for editor {}", editor);
        executor.add(editor, editorObsFuncApp(editor, text, mbFile, mbProject));
    }

    @Override public void updateEditor(SpoofaxEditor editor, String text, IFile file, IProject project,
        @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        final PPath mbFile = pathSrv.resolve(file);
        if(mbFile == null) {
            logger.error("Failed to set editor pipeline observer; cannot resolve Eclipse file {} to a path", file);
            return;
        }
        final PPath mbProject = pathSrv.resolve(project);
        if(mbProject == null) {
            logger.error("Failed to set editor pipeline observer; cannot resolve Eclipse project {} to a path",
                project);
            return;
        }

        logger.debug("Updating pipeline function for editor {}", editor);
        try {
            logger.debug("Executing pipeline function for editor {}...", editor);
            executor.updateAndExecute(editor, editorObsFuncApp(editor, text, mbFile, mbProject),
                PipelineCancel.cancelled(monitor));
        } finally {
            logger.debug("Done executing pipeline function for editor {}", editor);
        }
    }

    @Override public void removeEditor(SpoofaxEditor editor) {
        logger.debug("Unregistering pipeline function for editor {}", editor);
        executor.remove(editor);
    }


    @Override public void cleanAll(@Nullable IProgressMonitor monitor) throws CoreException {
        logger.debug("Cleaning all stored pipeline data");
        executor.dropStore();
        executor.dropCache();
        final WorkspaceUpdate update = workspaceUpdateFactory.create();
        update.addClearRec(root);
        update.updateMessagesSync(eclipseRoot, monitor);
    }


    private ObsFuncApp<? extends Serializable, Serializable> projectObsFuncApp(PPath project) {
        final FuncApp<processProject.Input, processProject.Output> app = pipeline.project(project, root);
        @SuppressWarnings("unchecked") final Function1<Serializable, Unit> obs =
            (Function1<Serializable, Unit>) observers.project(project);
        return new ObsFuncApp<>(app, obs);
    }

    private ObsFuncApp<? extends Serializable, Serializable> editorObsFuncApp(SpoofaxEditor editor, String text,
        PPath file, PPath project) {
        final FuncApp<processEditor.Input, processEditor.Output> app = pipeline.editor(text, file, project, root);
        @SuppressWarnings("unchecked") final Function1<Serializable, Unit> obs =
            (Function1<Serializable, Unit>) observers.editor(editor, text, file, project);
        return new ObsFuncApp<>(app, obs);
    }
}
