package mb.spoofax.lwb.eclipse;

import io.github.classgraph.ClassGraph;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.log.api.Logger;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.eclipsebundle.SpoofaxCompilerEclipseBundle;
import mb.spoofax.eclipse.EclipseResourceServiceComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.util.ResourceUtil;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageException;
import mb.tooling.eclipsebundle.ToolingEclipseBundle;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.util.LinkedHashSet;
import java.util.Map;

public class SpoofaxLwbBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxLwbPlugin.id + ".builder";

    private final Logger logger;

    public SpoofaxLwbBuilder() {
        this.logger = SpoofaxPlugin.getLoggerComponent().getLoggerFactory().create(getClass());
    }

    @Override
    protected @Nullable IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        if(kind == AUTO_BUILD) {
            return null; // Ignore automatics builds (for now?)
        }

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
        final ResourcePath rootDirectory = getResourcePath(eclipseProject);
        logger.debug("Running full language build of {}", rootDirectory);
        final PieComponent pieComponent = SpoofaxLwbLifecycleParticipant.getInstance().getPieComponent();
        try(final MixedSession session = pieComponent.getPie().newSession()) {
            topDownBuild(rootDirectory, session, monitor);
        } catch(ExecException e) {
            cancel(monitor);
            throw toCoreException(rootDirectory, e);
        }
    }

    private void incrBuild(IProject eclipseProject, IResourceDelta delta, @Nullable IProgressMonitor monitor) throws CoreException, InterruptedException {
        final ResourcePath rootDirectory = getResourcePath(eclipseProject);
        logger.debug("Running incremental language build of {}", rootDirectory);
        final PieComponent pieComponent = SpoofaxLwbLifecycleParticipant.getInstance().getPieComponent();
        final Task<Result<CompileLanguage.Output, CompileLanguageException>> compileTask = createCompileTask(rootDirectory);
        try(final MixedSession session = pieComponent.newSession()) {
            if(!pieComponent.getPie().hasBeenExecuted(compileTask)) {
                topDownBuild(rootDirectory, session, monitor);
            } else {
                bottomUpBuild(rootDirectory, delta, session, monitor);
            }
        } catch(ExecException e) {
            cancel(monitor);
            throw toCoreException(rootDirectory, e);
        }
    }


    private void topDownBuild(ResourcePath rootDirectory, MixedSession session, @Nullable IProgressMonitor monitor) throws InterruptedException, CoreException, ExecException {
        logger.debug("Top-down language build of {}", rootDirectory);
        final KeyedMessages messages = session.requireWithoutObserving(createCheckTask(rootDirectory));
        if(messages.containsError()) {
            logger.debug("Checking language specification revealed errors; skipping compilation");
            return;
        }
        final Result<CompileLanguage.Output, CompileLanguageException> result = session.require(createCompileTask(rootDirectory));
        handleCompileResult(rootDirectory, result, monitor);
    }

    private void bottomUpBuild(ResourcePath rootDirectory, IResourceDelta delta, MixedSession session, @Nullable IProgressMonitor monitor) throws InterruptedException, CoreException, ExecException {
        final LinkedHashSet<ResourceKey> changedResources = new LinkedHashSet<>();
        delta.accept((d) -> {
            changedResources.add(getResourcePath(d.getResource()));
            return true;
        });
        logger.debug("Bottom-up language build of {} with changed resources {}", rootDirectory, changedResources);
        final TopDownSession topDownSession = session.updateAffectedBy(changedResources);
        final Result<CompileLanguage.Output, CompileLanguageException> result = topDownSession.getOutput(createCompileTask(rootDirectory));
        handleCompileResult(rootDirectory, result, monitor);
    }


    private ResourcePath getResourcePath(IResource eclipseResource) {
        return new EclipseResourcePath(eclipseResource);
        // ResourceUtil.toFsPath(
    }


    private Task<KeyedMessages> createCheckTask(ResourcePath rootDirectory) {
        return SpoofaxLwbLifecycleParticipant.getInstance().getSpoofax3Compiler().component.getCheckLanguageSpecification().createTask(rootDirectory);
    }

    private Task<Result<CompileLanguage.Output, CompileLanguageException>> createCompileTask(ResourcePath rootDirectory) {
        final ClassGraph classGraph = new ClassGraph()
            .addClassLoader(SpoofaxLwbPlugin.class.getClassLoader())
            .addClassLoader(SpoofaxPlugin.class.getClassLoader())
            .addClassLoader(ToolingEclipseBundle.class.getClassLoader())
            .addClassLoader(SpoofaxCompilerEclipseBundle.class.getClassLoader()); // OPTO: only scan for classpath once?
        final CompileLanguage.Args args = CompileLanguage.Args.builder()
            .rootDirectory(rootDirectory)
            .additionalJavaClassPath(classGraph.getClasspathFiles())
            .build();
        return SpoofaxLwbLifecycleParticipant.getInstance().getSpoofax3Compiler().component.getCompileLanguage().createTask(args);
    }


    private void handleCompileResult(ResourcePath rootDirectory, Result<CompileLanguage.Output, CompileLanguageException> result, @Nullable IProgressMonitor monitor) throws CoreException {
        try {
            result.unwrap();
        } catch(CompileLanguageException e) {
            if(e.caseOf().javaCompilationFail_(true).otherwise_(false)) {
                // Don't cancel and throw in case of a Java compilation exception, so that the Eclipse Java compiler has a chance to run and show errors.
                logger.debug("Java compilation failed, but not cancelling the build to give ECJ a chance to run. Error: {}",
                    new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory).printExceptionToString(e));
            } else {
                cancel(monitor);
                throw toCoreException(rootDirectory, e);
            }
        }
    }

    private CoreException toCoreException(ResourcePath rootDirectory, Throwable e) {
        final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
        exceptionPrinter.addCurrentDirectoryContext(rootDirectory);
        final String message = exceptionPrinter.printExceptionToString(e);
        return new CoreException(new Status(IStatus.ERROR, SpoofaxLwbPlugin.id, IStatus.ERROR, message, null));
    }

    private void cancel(@Nullable IProgressMonitor monitor) {
        rememberLastBuiltState();
        if(monitor != null) monitor.setCanceled(true);
    }
}
