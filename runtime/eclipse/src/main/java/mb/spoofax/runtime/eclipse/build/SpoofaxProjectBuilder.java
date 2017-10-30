package mb.spoofax.runtime.eclipse.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.google.inject.Injector;

import mb.log.Logger;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.Executor;
import mb.pie.runtime.core.ObsFuncApp;
import mb.pie.runtime.core.PushingExecutor;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.pipeline.PipelineAdapter;
import mb.spoofax.runtime.eclipse.pipeline.WorkspaceUpdate;
import mb.spoofax.runtime.eclipse.pipeline.WorkspaceUpdateFactory;
import mb.spoofax.runtime.eclipse.util.StatusUtils;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.pie.PieSrv;
import mb.vfs.path.PPath;

public class SpoofaxProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxPlugin.id + ".builder";

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final PieSrv pieSrv;
    private final PipelineAdapter pipelineAdapter;
    private final WorkspaceUpdateFactory workspaceUpdateFactory;

    private final IWorkspaceRoot eclipseWorkspaceRoot;
    private final PPath workspaceRoot;


    public SpoofaxProjectBuilder() {
        final Injector injector = SpoofaxPlugin.spoofaxFacade().injector;
        this.logger = injector.getInstance(Logger.class).forContext(getClass());
        this.pathSrv = injector.getInstance(EclipsePathSrv.class);
        this.pieSrv = injector.getInstance(PieSrv.class);
        this.pipelineAdapter = injector.getInstance(PipelineAdapter.class);
        this.workspaceUpdateFactory = injector.getInstance(WorkspaceUpdateFactory.class);

        this.eclipseWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        this.workspaceRoot = pathSrv.resolveWorkspaceRoot();
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        final IProject currentEclipseProject = getProject();
        logger.info("Building project {}", currentEclipseProject);
        final ArrayList<PPath> changedPaths;
        final IResourceDelta delta = getDelta(currentEclipseProject);
        if(delta != null) {
            changedPaths = getChangedPaths(delta);
        } else {
            changedPaths = new ArrayList<>();
        }

        final WorkspaceUpdate update = workspaceUpdateFactory.create();
        final ArrayList<ObsFuncApp<Serializable, Serializable>> obsFuncApps =
            pipelineAdapter.workspaceObsFuncApps(update);

        logger.info("Executing pipeline...");
        try {
            final PushingExecutor pushingExecutor = pieSrv.getPushingExecutor(workspaceRoot, SpoofaxPlugin.useInMemoryStore);
            pushingExecutor.require(obsFuncApps, changedPaths);
        } catch(ExecException e) {
            logger.error("Pipeline execution failed", e);
            throw new CoreException(StatusUtils.buildFailure("Pipeline execution failed", e));
        }
        logger.info("Pipeline execution completed");

        update.updateMessagesSync(eclipseWorkspaceRoot, monitor);
        update.updateStyleAsync(monitor);

        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        final Executor executor = pieSrv.getPushingExecutor(workspaceRoot, SpoofaxPlugin.useInMemoryStore);
        executor.dropStore();
        executor.dropCache();

        final WorkspaceUpdate update = workspaceUpdateFactory.create();
        update.addClearRec(workspaceRoot);
        update.updateMessagesSync(eclipseWorkspaceRoot, monitor);

        forgetLastBuiltState();
    }

    @Override public ISchedulingRule getRule(int kind, Map<String, String> args) {
        return eclipseWorkspaceRoot;
    }


    private final ArrayList<PPath> getChangedPaths(IResourceDelta delta) throws CoreException {
        final ArrayList<PPath> changedPaths = new ArrayList<>();
        delta.accept(new IResourceDeltaVisitor() {
            @Override public boolean visit(IResourceDelta innerDelta) throws CoreException {
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
        return changedPaths;
    }
}
