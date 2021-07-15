package mb.spoofax.lwb.eclipse;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.log.api.Level;
import mb.log.api.Logger;
import mb.log.stream.LoggingOutputStream;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.OutTransient;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.MonitorCancelableToken;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageException;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.eclipse.util.ClassPathUtil;
import mb.spoofax.lwb.eclipse.util.JavaProjectUtil;
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

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.List;
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

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        final Pie pie = SpoofaxLwbLifecycleParticipant.getInstance().getPieComponent().getPie();
        pie.dropCallbacks();
        pie.dropStore();
        forgetLastBuiltState();
    }


    private void fullBuild(IProject eclipseProject, @Nullable IProgressMonitor monitor) throws CoreException, InterruptedException {
        JavaProjectUtil.configureProject(eclipseProject, monitor);
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
        JavaProjectUtil.configureProject(eclipseProject, monitor);
        final ResourcePath rootDirectory = getResourcePath(eclipseProject);
        logger.debug("Running incremental language build of {}", rootDirectory);
        final PieComponent pieComponent = SpoofaxLwbLifecycleParticipant.getInstance().getPieComponent();
        try(final MixedSession session = pieComponent.newSession()) {
            bottomUpBuild(rootDirectory, delta, session, monitor);
        } catch(ExecException e) {
            cancel(monitor);
            throw toCoreException(rootDirectory, e);
        }
    }


    private void topDownBuild(
        ResourcePath rootDirectory,
        MixedSession session,
        @Nullable IProgressMonitor monitor
    ) throws InterruptedException, CoreException, ExecException {
        logger.debug("Top-down language build of {}", rootDirectory);

        final KeyedMessages messages = session.require(createCheckTask(rootDirectory));
        if(messages.containsError()) {
            logger.debug("Checking language specification revealed errors; skipping compilation");
            new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory).printMessages(messages, new PrintStream(new LoggingOutputStream(logger, Level.Debug)));
            return;
        }

        final Result<CompileLanguage.Output, CompileLanguageException> result = session.require(createCompileTask(rootDirectory));
        handleCompileResult(rootDirectory, result, monitor);

        final OutTransient<Result<DynamicLanguage, ?>> dynamicLanguage = session.require(createDynamicLoadTask(rootDirectory));
        handleDynamicLoadResult(dynamicLanguage);
    }

    private void bottomUpBuild(
        ResourcePath rootDirectory,
        IResourceDelta delta,
        MixedSession session,
        @Nullable IProgressMonitor monitor
    ) throws InterruptedException, CoreException, ExecException {
        final LinkedHashSet<ResourceKey> changedResources = new LinkedHashSet<>();
        delta.accept((d) -> {
            final int kind = d.getKind();
            logger.debug(d.getResource().toString());
            if(kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED || kind == IResourceDelta.CHANGED) {
                changedResources.add(getResourcePath(d.getResource()));
                return true;
            }
            return false;
        });

        logger.debug("Bottom-up language build of {} with changed resources {}", rootDirectory, changedResources);
        final TopDownSession topDownSession = session.updateAffectedBy(changedResources);

        final MonitorCancelableToken cancelToken = new MonitorCancelableToken(monitor);
        final KeyedMessages messages = topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(createCheckTask(rootDirectory), cancelToken);
        if(messages.containsError()) {
            logger.debug("Checking language specification revealed errors; skipping handling of compile and dynamic load result");
            new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory).printMessages(messages, new PrintStream(new LoggingOutputStream(logger, Level.Debug)));
            return;
        }

        final Result<CompileLanguage.Output, CompileLanguageException> result = topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(createCompileTask(rootDirectory), cancelToken);
        handleCompileResult(rootDirectory, result, monitor);

        final OutTransient<Result<DynamicLanguage, ?>> dynamicLanguage = topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(createDynamicLoadTask(rootDirectory), cancelToken);
        handleDynamicLoadResult(dynamicLanguage);
    }

    private ResourcePath getResourcePath(IResource eclipseResource) {
        return new EclipseResourcePath(eclipseResource);
    }


    private Task<KeyedMessages> createCheckTask(ResourcePath rootDirectory) {
        return SpoofaxLwbLifecycleParticipant.getInstance().getSpoofax3Compiler().component.getCheckLanguageSpecification().createTask(rootDirectory);
    }

    private CompileLanguage.Args createCompileArgs(ResourcePath rootDirectory) {
        final List<File> classPath = ClassPathUtil.getClassPath();
        logger.trace("Using class path: {}", classPath);
        return CompileLanguage.Args.builder()
            .rootDirectory(rootDirectory)
            .additionalJavaClassPath(classPath)
            .additionalJavaAnnotationProcessorPath(classPath)
            .build();

    }

    private Task<Result<CompileLanguage.Output, CompileLanguageException>> createCompileTask(ResourcePath rootDirectory) {
        return SpoofaxLwbLifecycleParticipant.getInstance().getSpoofax3Compiler().component.getCompileLanguage().createTask(createCompileArgs(rootDirectory));
    }

    private Task<OutTransient<Result<DynamicLanguage, ?>>> createDynamicLoadTask(ResourcePath rootDirectory) {
        return SpoofaxLwbLifecycleParticipant.getInstance().getDynamicLoadingComponent().getDynamicLoad().createTask(createCompileArgs(rootDirectory));
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

    private void handleDynamicLoadResult(OutTransient<Result<DynamicLanguage, ?>> dynamicLanguage) {
        if(dynamicLanguage.isConsistent()) {
            dynamicLanguage.getValue().ifElse(
                l -> logger.debug("Possibly dynamically loaded language '{}'", l.getLanguageComponent().getLanguageInstance().getDisplayName()),
                e -> logger.debug("Dynamic load task failed", e)
            );
        } else {
            logger.debug("Dynamic language returned from dynamic load task is not consistent");
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
