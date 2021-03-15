package mb.spoofax.lwb.eclipse;

import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.log.api.Logger;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.lwb.compiler.CompileLanguageToJavaClassPath;
import mb.spoofax.lwb.compiler.CompileLanguageWithCfgToJavaClassPathException;
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.util.Map;

public class SpoofaxLwbBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxLwbPlugin.id + ".builder";

    private final Logger logger;
    private final Spoofax3Compiler spoofax3Compiler;

    public SpoofaxLwbBuilder() {
        this.logger = SpoofaxPlugin.getLoggerComponent().getLoggerFactory().create(getClass());
        this.spoofax3Compiler = SpoofaxLwbPlugin.getSpoofax3Compiler();
    }

    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
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
        }
        return null;
    }

    private void fullBuild(IProject eclipseProject, @Nullable IProgressMonitor monitor) throws CoreException, InterruptedException {
        final EclipseResourcePath project = new EclipseResourcePath(eclipseProject);
        try(final MixedSession session = spoofax3Compiler.pieComponent.getPie().newSession()) {
            final Result<CompileLanguageToJavaClassPath.Output, CompileLanguageWithCfgToJavaClassPathException> result =
                session.require(spoofax3Compiler.component.getCompileLanguageWithCfgToJavaClassPath().createTask(project));
            result.unwrap();
        } catch(ExecException | CompileLanguageWithCfgToJavaClassPathException e) {
            final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
            exceptionPrinter.addCurrentDirectoryContext(project);
            final String message = exceptionPrinter.printExceptionToString(e);
            throw new CoreException(new Status(IStatus.ERROR, SpoofaxLwbPlugin.id, IStatus.ERROR, message, null));
        }
    }

    private void incrBuild(IProject eclipseProject, IResourceDelta delta, @Nullable IProgressMonitor monitor) throws CoreException, InterruptedException {
        fullBuild(eclipseProject, monitor); // TODO: incremental build.
    }

    private void cancel(@Nullable IProgressMonitor monitor) {
        rememberLastBuiltState();
        if(monitor != null) monitor.setCanceled(true);
    }
}
