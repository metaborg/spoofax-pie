package mb.spoofax.eclipse.build;

import mb.log.api.Logger;
import mb.pie.api.ExecException;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.StatusUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import java.io.IOException;
import java.util.Map;

public abstract class SpoofaxProjectBuilder extends IncrementalProjectBuilder {
    private final EclipseLanguageComponent languageComponent;
    private final Logger logger;
    private final PieRunner pieRunner;

    public SpoofaxProjectBuilder(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;
        this.logger = SpoofaxPlugin.getPlatformComponent().getLoggerFactory().create(getClass());
        this.pieRunner = SpoofaxPlugin.getPlatformComponent().getPieRunner();
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
                    incrBuild(project, delta, monitor);
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
        pieRunner.fullBuild(languageComponent, eclipseProject, monitor);
    }

    private void incrBuild(IProject eclipseProject, IResourceDelta delta, @Nullable IProgressMonitor monitor) throws CoreException, ExecException, InterruptedException, IOException {
        pieRunner.incrementalBuild(languageComponent, eclipseProject, delta, monitor);
    }

    private void cancel(@Nullable IProgressMonitor monitor) {
        rememberLastBuiltState();
        if(monitor != null) monitor.setCanceled(true);
    }


    @Override
    protected void clean(@Nullable IProgressMonitor monitor) throws CoreException {
        final IProject project = getProject();
        try {
            pieRunner.clean(languageComponent, project, monitor);
        } catch(IOException e) {
            cancel(monitor);
            final String message = "Cleaning project '" + project + "' failed unexpectedly";
            logger.error(message);
            throw new CoreException(StatusUtil.error(message, e));
        }
    }
}
