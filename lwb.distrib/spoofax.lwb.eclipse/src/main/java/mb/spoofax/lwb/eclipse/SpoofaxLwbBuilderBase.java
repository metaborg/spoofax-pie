package mb.spoofax.lwb.eclipse;

import mb.common.util.ExceptionPrinter;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;

public abstract class SpoofaxLwbBuilderBase extends IncrementalProjectBuilder {
    protected abstract void topDownBuild(
        IProject project,
        EclipseResourcePath rootDirectory,
        MixedSession session,
        @Nullable IProgressMonitor monitor
    ) throws InterruptedException, CoreException, ExecException, IOException;

    protected abstract void bottomUpBuild(
        IProject project,
        EclipseResourcePath rootDirectory,
        IResourceDelta delta,
        MixedSession session,
        @Nullable IProgressMonitor monitor
    ) throws InterruptedException, CoreException, ExecException, IOException;


    @Override
    protected IProject[] build(int kind, Map<String, String> args, @Nullable IProgressMonitor monitor) throws CoreException {
        final IProject project = getProject();
        SpoofaxLwbNature.ensureCorrectNaturesAndBuilders(project, monitor);
        final ICoreRunnable runnable = new ICoreRunnable() {
            @Override public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    if(kind == FULL_BUILD) {
                        fullBuild(project, monitor);
                    } else {
                        final @Nullable IResourceDelta delta = getDelta(project);
                        if(delta == null) {
                            fullBuild(project, monitor);
                        } else {
                            incrBuild(project, delta, monitor);
                        }
                    }
                    project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                } catch(InterruptedException e) {
                    cancel(monitor);
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(runnable, project, IWorkspace.AVOID_UPDATE, monitor);
        return null;
    }


    protected void fullBuild(IProject project, @Nullable IProgressMonitor monitor) throws CoreException, InterruptedException {
        final EclipseResourcePath rootDirectory = getResourcePath(project);
        final PieComponent pieComponent = getPieComponent();
        try(final MixedSession session = pieComponent.getPie().newSession()) {
            topDownBuild(project, rootDirectory, session, monitor);
        } catch(ExecException | IOException e) {
            cancel(monitor);
            throw toCoreException(rootDirectory, e);
        }
    }

    protected void incrBuild(IProject project, IResourceDelta delta, @Nullable IProgressMonitor monitor) throws CoreException, InterruptedException {
        final EclipseResourcePath rootDirectory = getResourcePath(project);
        final PieComponent pieComponent = getPieComponent();
        try(final MixedSession session = pieComponent.newSession()) {
            bottomUpBuild(project, rootDirectory, delta, session, monitor);
        } catch(ExecException | IOException e) {
            cancel(monitor);
            throw toCoreException(rootDirectory, e);
        }
    }

    protected EclipseResourcePath getResourcePath(IResource eclipseResource) {
        return new EclipseResourcePath(eclipseResource);
    }

    protected PieComponent getPieComponent() {
        return SpoofaxPlugin.getStaticComponentManager().getComponentGroup("mb.spoofax.lwb").unwrap().getPieComponent();
    }


    protected void cancel(@Nullable IProgressMonitor monitor) {
        rememberLastBuiltState();
        if(monitor != null) monitor.setCanceled(true);
    }

    protected CoreException toCoreException(ResourcePath rootDirectory, Throwable e) {
        final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
        exceptionPrinter.addCurrentDirectoryContext(rootDirectory);
        final String message = exceptionPrinter.printExceptionToString(e);
        return new CoreException(new Status(IStatus.ERROR, SpoofaxLwbPlugin.id, IStatus.ERROR, message, null));
    }


    protected LinkedHashSet<ResourceKey> getChangedResources(IResourceDelta delta) throws CoreException {
        final LinkedHashSet<ResourceKey> changedResources = new LinkedHashSet<>();
        delta.accept((d) -> {
            final int kind = d.getKind();
            if(kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED || kind == IResourceDelta.CHANGED) {
                changedResources.add(getResourcePath(d.getResource()));
                return true;
            }
            return false;
        });
        return changedResources;
    }
}
