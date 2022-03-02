package mb.spoofax.lwb.eclipse;

import mb.cfg.eclipse.CfgEclipseParticipantFactory;
import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.esv.eclipse.EsvEclipseParticipantFactory;
import mb.log.api.Level;
import mb.log.api.Logger;
import mb.log.stream.LoggingOutputStream;
import mb.pie.api.ExecException;
import mb.pie.api.Interactivity;
import mb.pie.api.MixedSession;
import mb.pie.api.OutTransient;
import mb.pie.api.Pie;
import mb.pie.api.Session;
import mb.pie.api.TaskDef;
import mb.pie.api.TopDownSession;
import mb.pie.api.exec.CancelToken;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.eclipse.Sdf3EclipseParticipantFactory;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.MonitorCancelableToken;
import mb.spoofax.eclipse.pie.WorkspaceUpdate;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerComponent;
import mb.spoofax.lwb.compiler.definition.CompileLanguageDefinition;
import mb.spoofax.lwb.compiler.definition.CompileLanguageDefinitionException;
import mb.spoofax.lwb.dynamicloading.DynamicLoadException;
import mb.spoofax.lwb.dynamicloading.DynamicLoadGetBaseComponentManager;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;
import mb.spoofax.lwb.eclipse.util.JavaProjectUtil;
import mb.statix.eclipse.StatixEclipseParticipantFactory;
import mb.str.eclipse.StrategoEclipseParticipantFactory;
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
    private final DynamicComponentManager dynamicComponentManager;
    private final Set<Interactivity> compileTags;

    public SpoofaxLwbBuilder() {
        this.logger = SpoofaxPlugin.getLoggerComponent().getLoggerFactory().create(getClass());
        this.workspaceUpdateFactory = SpoofaxPlugin.getPlatformComponent().getWorkspaceUpdateFactory();
        final DynamicLoadingComponent dynamicLoadingComponent = SpoofaxLwbPlugin.getDynamicLoadingComponent();
        synchronized(this) {
            final DynamicLoadGetBaseComponentManager dynamicLoadGetBaseComponentManager = dynamicLoadingComponent.getDynamicLoadGetBaseComponentManager();
            if(!dynamicLoadGetBaseComponentManager.isSet()) { // HACK: set base component manager once
                dynamicLoadGetBaseComponentManager.set(SpoofaxPlugin.getStaticComponentManager());
            }
        }
        this.dynamicComponentManager = dynamicLoadingComponent.getDynamicComponentManager();
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

    private PieComponent getPieComponent() {
        return SpoofaxPlugin.getStaticComponentManager().getComponentGroup("mb.spoofax.lwb").unwrap().getPieComponent();
    }

    @Override protected void clean(@Nullable IProgressMonitor monitor) throws CoreException {
        final Pie pie = getPieComponent().getPie();
        try(MixedSession session = pie.newSession()) {
            session.dropCallbacks();
            session.dropStore();
        }

        final IProject eclipseProject = getProject();
        final ResourcePath rootDirectory = getResourcePath(eclipseProject);
        cleanCheckMessages(eclipseProject, rootDirectory, monitor);

        dynamicComponentManager.unloadFromCompiledSources(rootDirectory);

        forgetLastBuiltState();
    }

    @Override public ISchedulingRule getRule(int kind, Map<String, String> args) {
        return MultiRule.combine(getProject(), SpoofaxPlugin.getPlatformComponent().lifecycleParticipantManagerWriteLockRule());
    }

    private void fullBuild(IProject eclipseProject, @Nullable IProgressMonitor monitor) throws CoreException, InterruptedException {
        JavaProjectUtil.configureProject(eclipseProject, monitor);
        final ResourcePath rootDirectory = getResourcePath(eclipseProject);
        logger.debug("Running full language build of {}", rootDirectory);
        final PieComponent pieComponent = getPieComponent();
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
        final PieComponent pieComponent = getPieComponent();
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

        final KeyedMessages messages = session.require(SpoofaxLwbCompilerUtil.createCheckLanguageDefinitionTask(rootDirectory), cancelToken);
        updateCheckMessages(eclipseProject, rootDirectory, session, monitor);
        handleCheckResult(rootDirectory, messages);

        final Result<CompileLanguageDefinition.Output, CompileLanguageDefinitionException> result = session.require(SpoofaxLwbCompilerUtil.createCompileLanguageDefinitionTask(rootDirectory), cancelToken);
        handleCompileResult(rootDirectory, result, monitor);

        final OutTransient<Result<DynamicComponent, DynamicLoadException>> dynamicLanguage = session.require(SpoofaxLwbCompilerUtil.createDynamicLoadTask(rootDirectory), cancelToken);
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

        final KeyedMessages messages = topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(SpoofaxLwbCompilerUtil.createCheckLanguageDefinitionTask(rootDirectory), cancelToken);
        updateCheckMessages(eclipseProject, rootDirectory, topDownSession, monitor);
        handleCheckResult(rootDirectory, messages);

        final Result<CompileLanguageDefinition.Output, CompileLanguageDefinitionException> result = topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(SpoofaxLwbCompilerUtil.createCompileLanguageDefinitionTask(rootDirectory), cancelToken);
        handleCompileResult(rootDirectory, result, monitor);

        final OutTransient<Result<DynamicComponent, DynamicLoadException>> dynamicLanguage = topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(SpoofaxLwbCompilerUtil.createDynamicLoadTask(rootDirectory), cancelToken);
        handleDynamicLoadResult(dynamicLanguage, topDownSession);

        logger.debug("Deleting unobserved tasks");
        session.deleteUnobservedTasks(t -> true, (t, r) -> false);
    }

    private ResourcePath getResourcePath(IResource eclipseResource) {
        return new EclipseResourcePath(eclipseResource);
    }


    private void updateCheckMessages(IProject eclipseProject, ResourcePath rootDirectory, Session session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException, CoreException {
        final MonitorCancelableToken cancelToken = new MonitorCancelableToken(monitor);
        final SpoofaxLwbCompilerComponent spoofaxLwbCompilerComponent = SpoofaxLwbPlugin.getSpoofaxLwbCompilerComponent();
        final WorkspaceUpdate cfgUpdate = createUpdate(session, rootDirectory, cancelToken, getCfgIdentifiers(), spoofaxLwbCompilerComponent.getSpoofaxCfgCheck());
        final WorkspaceUpdate esvUpdate = createUpdate(session, rootDirectory, cancelToken, getEsvIdentifiers(), spoofaxLwbCompilerComponent.getSpoofaxEsvCheck());
        final WorkspaceUpdate sdf3Update = createUpdate(session, rootDirectory, cancelToken, getSdf3Identifiers(), spoofaxLwbCompilerComponent.getSpoofaxSdf3Check());
        final WorkspaceUpdate statixUpdate = createUpdate(session, rootDirectory, cancelToken, getStatixIdentifiers(), spoofaxLwbCompilerComponent.getSpoofaxStatixCheck());
        final WorkspaceUpdate strategoUpdate = createUpdate(session, rootDirectory, cancelToken, getStrategoIdentifiers(), spoofaxLwbCompilerComponent.getSpoofaxStrategoCheck());
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
        final WorkspaceUpdate cfgUpdate = createCleanUpdate(rootDirectory, getCfgIdentifiers());
        final WorkspaceUpdate esvUpdate = createCleanUpdate(rootDirectory, getEsvIdentifiers());
        final WorkspaceUpdate sdf3Update = createCleanUpdate(rootDirectory, getSdf3Identifiers());
        final WorkspaceUpdate statixUpdate = createCleanUpdate(rootDirectory, getStatixIdentifiers());
        final WorkspaceUpdate strategoUpdate = createCleanUpdate(rootDirectory, getStrategoIdentifiers());
        final ICoreRunnable runnable = runnableMonitor -> {
            cfgUpdate.createMarkerUpdate().run(runnableMonitor);
            esvUpdate.createMarkerUpdate().run(runnableMonitor);
            sdf3Update.createMarkerUpdate().run(runnableMonitor);
            statixUpdate.createMarkerUpdate().run(runnableMonitor);
            strategoUpdate.createMarkerUpdate().run(runnableMonitor);
        };
        ResourcesPlugin.getWorkspace().run(runnable, eclipseProject, IWorkspace.AVOID_UPDATE, monitor);
    }

    private EclipseIdentifiers getCfgIdentifiers() {
        return CfgEclipseParticipantFactory.getParticipant().getComponent().getEclipseIdentifiers();
    }

    private EclipseIdentifiers getEsvIdentifiers() {
        return EsvEclipseParticipantFactory.getParticipant().getComponent().getEclipseIdentifiers();
    }

    private EclipseIdentifiers getSdf3Identifiers() {
        return Sdf3EclipseParticipantFactory.getParticipant().getComponent().getEclipseIdentifiers();
    }

    private EclipseIdentifiers getStatixIdentifiers() {
        return StatixEclipseParticipantFactory.getParticipant().getComponent().getEclipseIdentifiers();
    }

    private EclipseIdentifiers getStrategoIdentifiers() {
        return StrategoEclipseParticipantFactory.getParticipant().getComponent().getEclipseIdentifiers();
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

    private void handleCheckResult(ResourcePath rootDirectory, KeyedMessages messages) throws CoreException {
        if(!messages.containsError()) return;
        rememberLastBuiltState(); // Ensure rebuild triggers the same error due to remembering previously changed files.
        final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
        exceptionPrinter.addCurrentDirectoryContext(rootDirectory);
        final KeyedMessages errorMessages = messages.filter(Message::isError);
        final String messagePrefix = "Checking language definition revealed errors. Fix the errors and build again";
        logger.info(messagePrefix);
        exceptionPrinter.printMessages(errorMessages, new PrintStream(new LoggingOutputStream(logger, Level.Info)));
        final String message = messagePrefix + ". The following errors occurred:\r\n" + exceptionPrinter.printMessagesToString(errorMessages);
        throw new CoreException(new Status(IStatus.ERROR, SpoofaxLwbPlugin.id, IStatus.ERROR, message, null));
    }

    private void handleCompileResult(ResourcePath rootDirectory, Result<CompileLanguageDefinition.Output, CompileLanguageDefinitionException> result, @Nullable IProgressMonitor monitor) throws CoreException {
        try {
            result.unwrap();
        } catch(CompileLanguageDefinitionException e) {
            rememberLastBuiltState(); // Ensure rebuild triggers the same error due to remembering previously changed files.
            final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
            exceptionPrinter.addCurrentDirectoryContext(rootDirectory);
            final String message;
            if(e.caseOf().javaCompilationFail_(true).otherwise_(false)) {
                message = "Java compilation failed. Fix the errors (if they are in your code, otherwise this is a bug) and build again";
            } else {
                message = "BUG: compiling language failed unexpectedly." + exceptionPrinter.printExceptionToString(e);
            }
            logger.info(message);
            throw new CoreException(new Status(IStatus.ERROR, SpoofaxLwbPlugin.id, IStatus.ERROR, message, e));
        }
    }

    private void handleDynamicLoadResult(OutTransient<Result<DynamicComponent, DynamicLoadException>> dynamicLanguage, Session session) throws ExecException {
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
