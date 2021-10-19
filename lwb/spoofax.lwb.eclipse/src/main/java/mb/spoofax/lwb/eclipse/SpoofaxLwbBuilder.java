package mb.spoofax.lwb.eclipse;

import mb.cfg.eclipse.CfgLanguageFactory;
import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.esv.eclipse.EsvLanguageFactory;
import mb.log.api.Level;
import mb.log.api.Logger;
import mb.log.stream.LoggingOutputStream;
import mb.pie.api.ExecException;
import mb.pie.api.Interactivity;
import mb.pie.api.MixedSession;
import mb.pie.api.OutTransient;
import mb.pie.api.Pie;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.TopDownSession;
import mb.pie.api.exec.CancelToken;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.eclipse.Sdf3LanguageFactory;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.MonitorCancelableToken;
import mb.spoofax.eclipse.pie.WorkspaceUpdate;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageException;
import mb.spoofax.lwb.compiler.dagger.Spoofax3CompilerComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageRegistry;
import mb.spoofax.lwb.eclipse.util.ClassPathUtil;
import mb.spoofax.lwb.eclipse.util.JavaProjectUtil;
import mb.statix.eclipse.StatixLanguageFactory;
import mb.str.eclipse.StrategoLanguageFactory;
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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SpoofaxLwbBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxLwbPlugin.id + ".builder";

    private final Logger logger;
    private final WorkspaceUpdate.Factory workspaceUpdateFactory;
    private final DynamicLanguageRegistry dynamicLanguageRegistry;
    private final Set<Interactivity> compileTags;

    public SpoofaxLwbBuilder() {
        this.logger = SpoofaxPlugin.getLoggerComponent().getLoggerFactory().create(getClass());
        this.workspaceUpdateFactory = SpoofaxPlugin.getPlatformComponent().getWorkspaceUpdateFactory();
        this.dynamicLanguageRegistry = SpoofaxLwbLifecycleParticipant.getInstance().getDynamicLoadingComponent().getDynamicLanguageRegistry();
        this.compileTags = Interactivity.NonInteractive.asSingletonSet();
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

    @Override protected void clean(@Nullable IProgressMonitor monitor) throws CoreException {
        final Pie pie = SpoofaxLwbLifecycleParticipant.getInstance().getPieComponent().getPie();
        try(MixedSession session = pie.newSession()) {
            session.dropCallbacks();
            session.dropStore();
        }

        final IProject eclipseProject = getProject();
        final ResourcePath rootDirectory = getResourcePath(eclipseProject);
        cleanCheckMessages(eclipseProject, rootDirectory, monitor);

        dynamicLanguageRegistry.unload(rootDirectory);

        forgetLastBuiltState();
    }

    @Override public ISchedulingRule getRule(int kind, Map<String, String> args) {
        return MultiRule.combine(getProject(), SpoofaxPlugin.getPlatformComponent().lifecycleParticipantManagerWriteLockRule());
    }

    private void fullBuild(IProject eclipseProject, @Nullable IProgressMonitor monitor) throws CoreException, InterruptedException {
        JavaProjectUtil.configureProject(eclipseProject, monitor);
        final ResourcePath rootDirectory = getResourcePath(eclipseProject);
        logger.debug("Running full language build of {}", rootDirectory);
        final PieComponent pieComponent = SpoofaxLwbLifecycleParticipant.getInstance().getPieComponent();
        try(final MixedSession session = pieComponent.getPie().newSession()) {
            topDownBuild(eclipseProject, rootDirectory, session, monitor);
        } catch(ExecException | IOException e) {
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
            bottomUpBuild(eclipseProject, rootDirectory, delta, session, monitor);
        } catch(ExecException | IOException e) {
            cancel(monitor);
            throw toCoreException(rootDirectory, e);
        }
    }


    private void topDownBuild(
        IProject eclipseProject,
        ResourcePath rootDirectory,
        MixedSession session,
        @Nullable IProgressMonitor monitor
    ) throws InterruptedException, CoreException, ExecException, IOException {
        logger.debug("Top-down language build of {}", rootDirectory);

        final MonitorCancelableToken cancelToken = new MonitorCancelableToken(monitor);

        final KeyedMessages messages = session.require(createCheckTask(rootDirectory), cancelToken);
        updateCheckMessages(eclipseProject, rootDirectory, session, monitor);
        if(messages.containsError()) {
            logger.debug("Checking language specification revealed errors; skipping compilation");
            new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory).printMessages(messages.filter(Message::isError), new PrintStream(new LoggingOutputStream(logger, Level.Debug)));
            return;
        }

        final Result<CompileLanguage.Output, CompileLanguageException> result = session.require(createCompileTask(rootDirectory), cancelToken);
        handleCompileResult(rootDirectory, result, monitor);

        final OutTransient<Result<DynamicLanguage, ?>> dynamicLanguage = session.require(createDynamicLoadTask(rootDirectory), cancelToken);
        handleDynamicLoadResult(dynamicLanguage, session);

        logger.debug("Deleting unobserved tasks");
        session.deleteUnobservedTasks(t -> true, (t, r) -> false);
    }

    private void bottomUpBuild(
        IProject eclipseProject,
        ResourcePath rootDirectory,
        IResourceDelta delta,
        MixedSession session,
        @Nullable IProgressMonitor monitor
    ) throws InterruptedException, CoreException, ExecException, IOException {
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

        final MonitorCancelableToken cancelToken = new MonitorCancelableToken(monitor);

        logger.debug("Bottom-up language build of {} with changed resources {}", rootDirectory, changedResources);
        final TopDownSession topDownSession = session.updateAffectedBy(changedResources, compileTags, cancelToken);

        final KeyedMessages messages = topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(createCheckTask(rootDirectory), cancelToken);
        updateCheckMessages(eclipseProject, rootDirectory, topDownSession, monitor);
        if(messages.containsError()) {
            logger.debug("Checking language specification revealed errors; skipping handling of compile and dynamic load result");
            new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory).printMessages(messages.filter(Message::isError), new PrintStream(new LoggingOutputStream(logger, Level.Debug)));
            return;
        }

        final Result<CompileLanguage.Output, CompileLanguageException> result = topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(createCompileTask(rootDirectory), cancelToken);
        handleCompileResult(rootDirectory, result, monitor);

        final OutTransient<Result<DynamicLanguage, ?>> dynamicLanguage = topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(createDynamicLoadTask(rootDirectory), cancelToken);
        handleDynamicLoadResult(dynamicLanguage, topDownSession);

        logger.debug("Deleting unobserved tasks");
        session.deleteUnobservedTasks(t -> true, (t, r) -> false);
    }

    private ResourcePath getResourcePath(IResource eclipseResource) {
        return new EclipseResourcePath(eclipseResource);
    }


    private Task<KeyedMessages> createCheckTask(ResourcePath rootDirectory) {
        return SpoofaxLwbLifecycleParticipant.getInstance().getSpoofax3Compiler().component.getCheckLanguageSpecification().createTask(rootDirectory);
    }

    private CompileLanguage.Args createCompileArgs(ResourcePath rootDirectory) {
        logger.trace("Using class path: {}", ClassPathUtil.getClassPath());
        return CompileLanguage.Args.builder()
            .rootDirectory(rootDirectory)
            .addJavaClassPathSuppliers(ClassPathUtil.getClassPathSupplier())
            .addJavaAnnotationProcessorPathSuppliers(ClassPathUtil.getClassPathSupplier())
            .build();

    }

    private Task<Result<CompileLanguage.Output, CompileLanguageException>> createCompileTask(ResourcePath rootDirectory) {
        return SpoofaxLwbLifecycleParticipant.getInstance().getSpoofax3Compiler().component.getCompileLanguage().createTask(createCompileArgs(rootDirectory));
    }

    private Task<OutTransient<Result<DynamicLanguage, ?>>> createDynamicLoadTask(ResourcePath rootDirectory) {
        return SpoofaxLwbLifecycleParticipant.getInstance().getDynamicLoadingComponent().getDynamicLoad().createTask(createCompileArgs(rootDirectory));
    }


    private void updateCheckMessages(IProject eclipseProject, ResourcePath rootDirectory, Session session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException, CoreException {
        final MonitorCancelableToken cancelToken = new MonitorCancelableToken(monitor);
        final Spoofax3CompilerComponent spoofax3CompilerComponent = SpoofaxLwbLifecycleParticipant.getInstance().getSpoofax3Compiler().component;
        final WorkspaceUpdate cfgUpdate = createUpdate(session, rootDirectory, cancelToken, CfgLanguageFactory.getLanguage().getComponent().getEclipseIdentifiers(), spoofax3CompilerComponent.getCfgCheck());
        final WorkspaceUpdate esvUpdate = createUpdate(session, rootDirectory, cancelToken, EsvLanguageFactory.getLanguage().getComponent().getEclipseIdentifiers(), spoofax3CompilerComponent.getCheckEsv());
        final WorkspaceUpdate sdf3Update = createUpdate(session, rootDirectory, cancelToken, Sdf3LanguageFactory.getLanguage().getComponent().getEclipseIdentifiers(), spoofax3CompilerComponent.getCheckSdf3());
        final WorkspaceUpdate statixUpdate = createUpdate(session, rootDirectory, cancelToken, StatixLanguageFactory.getLanguage().getComponent().getEclipseIdentifiers(), spoofax3CompilerComponent.getCheckStatix());
        final WorkspaceUpdate strategoUpdate = createUpdate(session, rootDirectory, cancelToken, StrategoLanguageFactory.getLanguage().getComponent().getEclipseIdentifiers(), spoofax3CompilerComponent.getCheckStratego());
        final ICoreRunnable runnable = runnableMonitor -> {
            cfgUpdate.createMarkerUpdate().run(runnableMonitor);
            esvUpdate.createMarkerUpdate().run(runnableMonitor);
            sdf3Update.createMarkerUpdate().run(runnableMonitor);
            statixUpdate.createMarkerUpdate().run(runnableMonitor);
            strategoUpdate.createMarkerUpdate().run(runnableMonitor);
        };
        ResourcesPlugin.getWorkspace().run(runnable, eclipseProject, IWorkspace.AVOID_UPDATE, monitor);
    }

    private void cleanCheckMessages(IProject eclipseProject, ResourcePath rootDirectory, @Nullable IProgressMonitor monitor) throws CoreException {
        final WorkspaceUpdate cfgUpdate = createCleanUpdate(rootDirectory, CfgLanguageFactory.getLanguage().getComponent().getEclipseIdentifiers());
        final WorkspaceUpdate esvUpdate = createCleanUpdate(rootDirectory, EsvLanguageFactory.getLanguage().getComponent().getEclipseIdentifiers());
        final WorkspaceUpdate sdf3Update = createCleanUpdate(rootDirectory, Sdf3LanguageFactory.getLanguage().getComponent().getEclipseIdentifiers());
        final WorkspaceUpdate statixUpdate = createCleanUpdate(rootDirectory, StatixLanguageFactory.getLanguage().getComponent().getEclipseIdentifiers());
        final WorkspaceUpdate strategoUpdate = createCleanUpdate(rootDirectory, StrategoLanguageFactory.getLanguage().getComponent().getEclipseIdentifiers());
        final ICoreRunnable runnable = runnableMonitor -> {
            cfgUpdate.createMarkerUpdate().run(runnableMonitor);
            esvUpdate.createMarkerUpdate().run(runnableMonitor);
            sdf3Update.createMarkerUpdate().run(runnableMonitor);
            statixUpdate.createMarkerUpdate().run(runnableMonitor);
            strategoUpdate.createMarkerUpdate().run(runnableMonitor);
        };
        ResourcesPlugin.getWorkspace().run(runnable, eclipseProject, IWorkspace.AVOID_UPDATE, monitor);
    }

    private WorkspaceUpdate createUpdate(Session session, ResourcePath rootDirectory, CancelToken cancelToken, EclipseIdentifiers identifiers, TaskDef<ResourcePath, KeyedMessages> taskDef) throws ExecException, InterruptedException {
        final KeyedMessages messages = session.require(taskDef.createTask(rootDirectory), cancelToken);
        final WorkspaceUpdate update = workspaceUpdateFactory.create(identifiers);
        update.replaceMessages(messages, rootDirectory);
        return update;
    }

    private WorkspaceUpdate createCleanUpdate(ResourcePath rootDirectory, EclipseIdentifiers identifiers) {
        final WorkspaceUpdate update = workspaceUpdateFactory.create(identifiers);
        update.clearMessages(rootDirectory, true);
        return update;
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

    private void handleDynamicLoadResult(OutTransient<Result<DynamicLanguage, ?>> dynamicLanguage, Session session) throws ExecException {
        if(dynamicLanguage.isConsistent()) {
            dynamicLanguage.getValue().ifThrowingElse(
                l -> {
                    logger.debug("Possibly dynamically loaded language '{}'", l);
                    // Run a bottom-up build in the dynamically loaded language, using the resources that were provided
                    // during the build of the language, to make the tasks of the language up-to-date.
                    try(final MixedSession languageSession = l.getPieComponent().newSession()) {
                        languageSession.updateAffectedBy(session.getProvidedResources());
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                },
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
