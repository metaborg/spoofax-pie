package mb.spoofax.eclipse.build;

import mb.log.api.Logger;
import mb.pie.api.ExecException;
import mb.resource.ResourceKey;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.resource.EclipseResource;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.util.StatusUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SpoofaxProjectBuilder extends IncrementalProjectBuilder {
    private final EclipseLanguageComponent languageComponent;
    private final Logger logger;
    private final PieRunner pieRunner;

    public SpoofaxProjectBuilder(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;
        this.logger = SpoofaxPlugin.getComponent().getLoggerFactory().create(getClass());
        this.pieRunner = SpoofaxPlugin.getComponent().getPieRunner();
    }

    @Override
    protected @Nullable IProject[] build(int kind, @Nullable Map<String, String> args, @Nullable IProgressMonitor monitor) throws CoreException {
        final IProject project = getProject();
        try {
            if(kind == FULL_BUILD) {
                fullBuild(project, monitor);
            } else {
                final @Nullable IResourceDelta delta = getDelta(project);
                if(delta == null) {
                    fullBuild(project, monitor);
                } else {
                    incrBuild(delta, monitor);
                }
            }
        } catch(InterruptedException e) {
            cancel(monitor);
        } catch(IOException | ExecException | CoreException e) {
            cancel(monitor);
            final String message = "Building project '" + project + "' failed unexpectedly";
            logger.error(message);
            throw new CoreException(StatusUtil.error(message, e));
        }
        return null;
    }

    private void fullBuild(IProject eclipseProject, @Nullable IProgressMonitor monitor) throws IOException, ExecException, InterruptedException {
        final EclipseResource project = new EclipseResource(eclipseProject);
        final HashSet<ResourceKey> allResourceKeys = project
            .walk()
            .map(EclipseResource::getKey)
            .collect(Collectors.toCollection(HashSet::new));
        pieRunner.incrementalBuild(languageComponent, allResourceKeys, monitor);
    }

    private void incrBuild(IResourceDelta delta, @Nullable IProgressMonitor monitor) throws CoreException, ExecException, InterruptedException {
        final HashSet<ResourceKey> changedResourceKeys = new HashSet<>();
        delta.accept((d) -> {
            changedResourceKeys.add(new EclipseResourcePath(d.getResource()));
            return true;
        });
        pieRunner.incrementalBuild(languageComponent, changedResourceKeys, monitor);
    }

    private void cancel(@Nullable IProgressMonitor monitor) {
        rememberLastBuiltState();
        if(monitor != null) monitor.setCanceled(true);
    }


    @Override
    protected void clean(@Nullable IProgressMonitor monitor) throws CoreException {
        // TODO: clean
    }
}
