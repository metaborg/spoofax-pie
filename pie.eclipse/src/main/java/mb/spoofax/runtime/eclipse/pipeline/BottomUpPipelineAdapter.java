package mb.spoofax.runtime.eclipse.pipeline;

import com.google.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mb.log.api.Logger;
import mb.pie.api.*;
import mb.pie.api.exec.BottomUpExecutor;
import mb.pie.api.exec.Cancelled;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.store.lmdb.LMDBStoreKt;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.pie.taskdefs.guice.GuiceTaskDefsKt;
import mb.pie.vfs.path.PPath;
import mb.spoofax.pie.LogLoggerKt;
import mb.spoofax.pie.SpoofaxPipeline;
import mb.spoofax.pie.generated.processContainer;
import mb.spoofax.pie.generated.processDocumentWithText;
import mb.spoofax.pie.processing.ContainerResult;
import mb.spoofax.pie.processing.DocumentResult;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class BottomUpPipelineAdapter implements PipelineAdapter {
    private final PipelineObservers observers;
    private final PipelinePathChanges pathChanges;
    private final SpoofaxPipeline pipeline;

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final WorkspaceUpdateFactory workspaceUpdateFactory;

    private final IWorkspaceRoot eclipseRoot;
    private final PPath root;
    private final Pie pie;
    private final BottomUpExecutor executor;

    private final HashMap<PPath, TaskKey> projectKeys = new HashMap<>();
    private final HashMap<SpoofaxEditor, TaskKey> editorKeys = new HashMap<>();


    @Inject public BottomUpPipelineAdapter(PipelineObservers observers, PipelinePathChanges pathChanges, Logger logger,
        EclipsePathSrv pathSrv, SpoofaxPipeline pipeline, GuiceTaskDefs taskDefs,
        WorkspaceUpdateFactory workspaceUpdateFactory) {
        this.observers = observers;
        this.pathChanges = pathChanges;
        this.pipeline = pipeline;

        this.logger = logger.forContext(getClass());
        this.pathSrv = pathSrv;
        this.workspaceUpdateFactory = workspaceUpdateFactory;

        this.eclipseRoot = ResourcesPlugin.getWorkspace().getRoot();
        this.root = pathSrv.resolve(eclipseRoot);
        final PieBuilder pieBuilder = new PieBuilderImpl();
        LogLoggerKt.withMbLogger(pieBuilder, logger);
        GuiceTaskDefsKt.withGuiceTaskDefs(pieBuilder, taskDefs);
        final File lmdbStoreDir = pathSrv.localPath(this.root.resolve(".pie"));
        if(lmdbStoreDir == null) {
            throw new RuntimeException("Could not get local filesystem path to LMDB store location " + lmdbStoreDir
                + "; it does not reside on the local filesystem");
        }
        LMDBStoreKt.withLMDBStore(pieBuilder, lmdbStoreDir);
        this.pie = pieBuilder.build();
        this.executor = pie.getBottomUpExecutor();
    }


    @Override public void addProject(IProject project) {
        final PPath mbProject = pathSrv.resolve(project);
        if(mbProject == null) {
            logger.error("Failed to set pipeline observer; cannot resolve Eclipse project {} to a path", project);
            return;
        }

        logger.debug("Setting pipeline observer for project {}", project);
        final Task<processContainer.Input, ContainerResult> task = containerTask(mbProject);
        final TaskKey key = task.key();
        final TaskKey prevKey = projectKeys.put(mbProject, key);
        if(prevKey != null) {
            executor.removeObserver(prevKey);
        }
        executor.setObserver(key, containerObs(mbProject));
    }

    @Override public void buildProject(IProject project, int buildKind, @Nullable IResourceDelta delta,
        @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException, CoreException {
        final PPath mbProject = pathSrv.resolve(project);
        if(mbProject == null) {
            logger.error("Failed to build project; cannot resolve Eclipse project {} to a path", project);
            return;
        }

        final Task<processContainer.Input, ContainerResult> task = containerTask(mbProject);
        final TaskKey key = task.key();
        final Cancelled cancelled = PipelineCancel.cancelled(monitor);

        if(!executor.hasBeenRequired(key) || buildKind == IncrementalProjectBuilder.FULL_BUILD || delta == null) {
            logger.debug("Building project {} from scratch...", project);
            executor.requireTopDown(task, cancelled);
            logger.debug("Done building project {} from scratch", project);
        } else {
            logger.debug("Building project {} incrementally...", project);
            final HashSet<PPath> changedPaths = pathChanges.changedPaths(delta);
            executor.requireBottomUp(changedPaths, cancelled);
            logger.debug("Done building project {} incrementally", project);
        }
    }

    @Override public void removeProject(IProject project) {
        final PPath mbProject = pathSrv.resolve(project);
        if(mbProject == null) {
            logger.error("Failed to remove pipeline observer; cannot resolve Eclipse project {} to a path", project);
            return;
        }
        final TaskKey key = projectKeys.get(mbProject);
        if(key == null) {
            logger.error("Failed to remove pipeline observer; no task key for project {}", mbProject);
            return;
        }
        logger.debug("Removing pipeline observer for project {}", project);
        executor.removeObserver(key);
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

        logger.debug("Setting pipeline observer for editor {}", editor);
        final Task<processDocumentWithText.Input, DocumentResult> task = documentWithTextTask(text, mbFile, mbProject);
        final TaskKey key = task.key();
        final TaskKey prevKey = editorKeys.put(editor, key);
        if(prevKey != null) {
            executor.removeObserver(prevKey);
        }
        executor.setObserver(key, documentWithTextObs(editor, text, mbFile, mbProject));
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

        logger.debug("Updating pipeline observer for editor {}", editor);
        final Task<processDocumentWithText.Input, DocumentResult> task = documentWithTextTask(text, mbFile, mbProject);
        final TaskKey key = task.key();
        final TaskKey prevKey = editorKeys.put(editor, key);
        if(prevKey != null) {
            executor.removeObserver(prevKey);
        }
        executor.setObserver(key, documentWithTextObs(editor, text, mbFile, mbProject));

        try {
            logger.debug("Executing pipeline function for editor {}...", editor);
            executor.requireTopDown(task, PipelineCancel.cancelled(monitor));
        } finally {
            logger.debug("Done executing pipeline function for editor {}", editor);
        }
    }

    @Override public void removeEditor(SpoofaxEditor editor) {
        final TaskKey key = editorKeys.get(editor);
        if(key == null) {
            logger.error("Failed to remove pipeline observer; no task key for editor {}", editor);
        }
        logger.debug("Removing pipeline observer for editor {}", editor);
        executor.removeObserver(key);
    }


    @Override public void cleanAll(@Nullable IProgressMonitor monitor) throws CoreException {
        logger.debug("Cleaning all stored pipeline data");
        pie.dropStore();
        final WorkspaceUpdate update = workspaceUpdateFactory.create();
        update.addClearRec(root);
        update.update(WorkspaceUpdate.lock, monitor);
    }


    private Task<processContainer.Input, ContainerResult> containerTask(PPath container) {
        return pipeline.container(container, root);
    }

    @SuppressWarnings("unchecked") private Function1<Serializable, Unit> containerObs(PPath container) {
        return (Function1<Serializable, Unit>) observers.container(container);
    }

    private Task<processDocumentWithText.Input, DocumentResult> documentWithTextTask(String text, PPath document,
        PPath container) {
        return pipeline.documentWithText(document, container, root, text);
    }

    @SuppressWarnings("unchecked") private Function1<Serializable, Unit> documentWithTextObs(SpoofaxEditor editor,
        String text, PPath document, PPath container) {
        return (Function1<Serializable, Unit>) observers.editor(editor, text, document, container);
    }
}
