package mb.spoofax.runtime.eclipse.pipeline;

import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.inject.Inject;

import kotlin.Unit;
import mb.log.Logger;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.ObsFuncApp;
import mb.pie.runtime.core.PushingExecutor;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.nature.SpoofaxNature;
import mb.spoofax.runtime.eclipse.util.NatureUtils;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution;
import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.style.Styling;
import mb.spoofax.runtime.pie.PieSrv;
import mb.spoofax.runtime.pie.builder.SpoofaxPipeline;
import mb.util.async.Cancelled;
import mb.util.async.NullCancelled;
import mb.vfs.path.PPath;

public class PipelineAdapter implements IResourceChangeListener {
    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final WorkspaceUpdateFactory workspaceUpdateFactory;

    private final IWorkspaceRoot eclipseWorkspaceRoot;
    private final PPath workspaceRoot;
    private final PushingExecutor pushingExecutor;


    @Inject public PipelineAdapter(Logger logger, EclipsePathSrv pathSrv, PieSrv pieSrv,
        WorkspaceUpdateFactory workspaceUpdateFactory) {
        this.logger = logger.forContext(getClass());
        this.pathSrv = pathSrv;
        this.workspaceUpdateFactory = workspaceUpdateFactory;

        this.eclipseWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        this.workspaceRoot = pathSrv.resolve(eclipseWorkspaceRoot);

        this.pushingExecutor = pieSrv.getPushingExecutor(workspaceRoot, SpoofaxPlugin.useInMemoryStore);
    }


    public void initialize() throws CoreException {
        addInitialProjects();
        registerResourceChangeListener();
    }


    public void addProject(IProject eclipseProject) {
        final PPath project = pathSrv.resolve(eclipseProject);
        if(project == null) {
            logger.error("Failed to add pipeline function; cannot resolve Eclipse project {} to a path",
                eclipseProject);
            return;
        }
        logger.debug("Registering pipeline function for project {}", eclipseProject);
        pushingExecutor.add(eclipseProject, projectObsFuncApp(project));
    }

    private void addInitialProjects() throws CoreException {
        for(IProject project : eclipseWorkspaceRoot.getProjects()) {
            if(project.isAccessible() && NatureUtils.exists(SpoofaxNature.id, project)) {
                addProject(project);
            }
        }
    }

    public void removeProject(IProject eclipseProject) {
        logger.debug("Unregistering pipeline function for project {}", eclipseProject);
        pushingExecutor.remove(eclipseProject);
    }


    public void addEditor(SpoofaxEditor editor, String text, PPath file, PPath project) {
        logger.debug("Registering pipeline function for editor {}", editor);
        pushingExecutor.add(editor, editorObsFuncApp(editor, text, file, project));
    }

    public void updateEditor(SpoofaxEditor editor, String text, PPath file, PPath project) {
        logger.debug("Updating pipeline function for editor {}", editor);
        pushingExecutor.update(editor, editorObsFuncApp(editor, text, file, project));
    }

    public void updateEditorAndExecute(SpoofaxEditor editor, String text, PPath file, PPath project,
        @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.debug("Updating pipeline function for editor {}", editor);
        try {
            logger.debug("Executing pipeline function for editor {}...", editor);
            pushingExecutor.updateAndExecute(editor, editorObsFuncApp(editor, text, file, project),
                cancellationToken(monitor));
        } finally {
            logger.debug("Done executing pipeline function for editor {}", editor);
        }
    }

    public void removeEditor(SpoofaxEditor editor) {
        logger.debug("Unregistering pipeline function for editor {}", editor);
        pushingExecutor.remove(editor);
    }


    public void dirtyFlag(IResourceDelta delta) throws CoreException {
        logger.debug("Processing resource delta");
        final ArrayList<PPath> changedPaths = new ArrayList<>();
        delta.accept(new IResourceDeltaVisitor() {
            public boolean visit(IResourceDelta innerDelta) throws CoreException {
                final int kind = innerDelta.getKind();
                switch(kind) {
                    case IResourceDelta.ADDED:
                    case IResourceDelta.REMOVED:
                    case IResourceDelta.CHANGED: {
                        final IResource resource = innerDelta.getResource();
                        if(!(resource.getType() != IResource.FILE && kind == IResourceDelta.CHANGED)) {
                            final PPath path = pathSrv.resolve(resource);
                            changedPaths.add(path);
                        }
                        break;
                    }
                    case IResourceDelta.NO_CHANGE:
                    case IResourceDelta.ADDED_PHANTOM:
                    case IResourceDelta.REMOVED_PHANTOM:
                    default:
                        break;
                }
                return true;
            }
        });
        pushingExecutor.pathsChanged(changedPaths);
        logger.debug("Dirty flagging...");
        pushingExecutor.dirtyFlag();
        logger.debug("Done dirty flagging");
    }


    public void executeAll(@Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        try {
            logger.debug("Executing all pipeline functions...");
            pushingExecutor.executeAll(cancellationToken(monitor));
        } finally {
            logger.debug("Done executing all pipeline functions");
        }
    }


    public void cleanAll() throws CoreException {
        logger.debug("Cleaning all stored pipeline data");

        pushingExecutor.dropStore();
        pushingExecutor.dropCache();

        final WorkspaceUpdate update = workspaceUpdateFactory.create();
        update.addClearRec(workspaceRoot);
        update.updateMessagesSync(eclipseWorkspaceRoot, null);
    }


    public void registerResourceChangeListener() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
            IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
    }

    @Override public void resourceChanged(IResourceChangeEvent event) {
        try {
            switch(event.getType()) {
                case IResourceChangeEvent.PRE_CLOSE: {
                    final IProject project = (IProject) event.getResource();
                    if(NatureUtils.exists(SpoofaxNature.id, project)) {
                        logger.debug("Spoofax project {} was closed", project);
                        removeProject(project);
                    }
                    break;
                }
                case IResourceChangeEvent.PRE_DELETE: {
                    final IProject project = (IProject) event.getResource();
                    if(NatureUtils.exists(SpoofaxNature.id, project)) {
                        logger.debug("Spoofax project {} was deleted", project);
                        removeProject(project);
                    }
                    break;
                }
                case IResourceChangeEvent.POST_CHANGE:
                    event.getDelta().accept(delta -> {
                        // Only match Spoofax projects.
                        final IResource resource = delta.getResource();
                        if(resource.getType() != IResource.PROJECT) {
                            return true; // Recurse into projects of workspace.
                        }
                        final IProject project = (IProject) resource;
                        if(!NatureUtils.exists(SpoofaxNature.id, project)) {
                            return false; // Never recurse into projects, since projects cannot be nested.
                        }

                        // Determine kind of change.
                        switch(delta.getKind()) {
                            case IResourceDelta.ADDED: {
                                logger.debug("Spoofax project {} was added", project);
                                if(project.isAccessible()) {
                                    addProject(project);
                                }
                                break;
                            }
                            case IResourceDelta.CHANGED: {
                                if((delta.getFlags() & IResourceDelta.OPEN) != 0) {
                                    if(project.isAccessible()) { // Project was opened
                                        logger.debug("Spoofax project {} was opened", project);
                                        addProject(project);
                                    }
                                }
                                break;
                            }
                        }
                        return false; // Never recurse into projects, since projects cannot be nested.
                    });
            }
        } catch(CoreException e) {
            logger.error("Failed to process resource delta", e);
        }
    }


    private ObsFuncApp<Serializable, Serializable> projectObsFuncApp(PPath project) {
        return SpoofaxPipeline.INSTANCE.processProjectObsFunApp(project, workspaceRoot, (projectResult) -> {
            final WorkspaceUpdate update = workspaceUpdateFactory.create();
            update.addClearRec(project);
            if(projectResult != null) {
                projectResult.component1().stream().forEach((result) -> {
                    result.component1().stream().forEach((fileResult) -> {
                        final PPath file = fileResult.component1();
                        final ArrayList<Msg> messages = fileResult.component3();
                        update.addMessages(file, messages);
                    });
                    final ArrayList<@Nullable ? extends ConstraintSolverSolution> solutions = result.component2();
                    for(@Nullable ConstraintSolverSolution solution : solutions) {
                        if(solution != null) {
                            update.addMessages(solution.getFileMessages());
                            update.addMessages(solution.getFileUnsolvedMessages());
                            update.addMessages(project, solution.getProjectMessages());
                            update.addMessages(project, solution.getProjectUnsolvedMessages());
                        }
                    }
                });
                projectResult.component2().stream().forEach((result) -> {
                    final PPath file = result.component1();
                    final ArrayList<Msg> messages = result.component3();
                    update.addMessages(file, messages);
                });
            }
            try {
                update.updateMessagesSync(eclipseWorkspaceRoot, null);
            } catch(CoreException e) {
                logger.error("Failed to update messages for project {}", e, project);
            }
            update.updateStyleAsync(null);
            return Unit.INSTANCE;
        });
    }

    private ObsFuncApp<Serializable, Serializable> editorObsFuncApp(SpoofaxEditor editor, String text, PPath file,
        PPath project) {
        return SpoofaxPipeline.INSTANCE.processEditorObsFunApp(text, file, project, workspaceRoot, (output) -> {
            final WorkspaceUpdate update = workspaceUpdateFactory.create();
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
                    update.addMessagesFiltered(solution.getFileMessages(), file);
                    update.addMessagesFiltered(solution.getFileUnsolvedMessages(), file);
                }
            } else {
                update.removeStyle(editor, text.length());
            }
            try {
                update.updateMessagesSync(null, null);
            } catch(CoreException e) {
                logger.error("Failed to update messages for project {}", e, project);
            }
            update.updateStyleAsync(null);
            return Unit.INSTANCE;
        });
    }

    private Cancelled cancellationToken(@Nullable IProgressMonitor monitor) {
        if(monitor != null) {
            return new MonitorCancelToken(monitor);
        } else {
            return new NullCancelled();
        }
    }
}
