package mb.spoofax.lwb.eclipse;

import mb.cfg.Dependency;
import mb.cfg.DependencyKind;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.log.api.Logger;
import mb.pie.api.ExecException;
import mb.pie.api.Interactivity;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.MonitorCancelableToken;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IDynamicReferenceProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Incremental project builder that updates the project references of projects.
 */
public class SpoofaxLwbProjectReferencesBuilder extends SpoofaxLwbBuilderBase implements IDynamicReferenceProvider {
    public static final String id = SpoofaxLwbPlugin.id + ".builder.project.references";

    private final Logger logger;
    private final Set<Interactivity> updateTags;

    public SpoofaxLwbProjectReferencesBuilder() {
        this.logger = SpoofaxPlugin.getLoggerComponent().getLoggerFactory().create(getClass());
        this.updateTags = Interactivity.Interactive.asSingletonSet();
    }


    @Override
    protected void topDownBuild(
        IProject project,
        EclipseResourcePath rootDirectory,
        MixedSession session,
        @Nullable IProgressMonitor monitor
    ) throws InterruptedException, CoreException, ExecException {
        logger.debug("Top-down project references update for {}", rootDirectory);
        final MonitorCancelableToken cancelToken = new MonitorCancelableToken(monitor);
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result = session.require(createTask(rootDirectory), cancelToken);
        final ArrayList<IProject> projectReferences = getProjectReferences(rootDirectory, result);
        setProjectReferences(project, projectReferences, monitor);
    }

    @Override
    protected void bottomUpBuild(
        IProject project,
        EclipseResourcePath rootDirectory,
        IResourceDelta delta,
        MixedSession session,
        @Nullable IProgressMonitor monitor
    ) throws InterruptedException, CoreException, ExecException {
        final LinkedHashSet<ResourceKey> changedResources = getChangedResources(delta);
        logger.debug("Bottom-up project references update for {} with changed resources {}", rootDirectory, changedResources);
        final MonitorCancelableToken cancelToken = new MonitorCancelableToken(monitor);
        final TopDownSession topDownSession = session.updateAffectedBy(changedResources, updateTags, cancelToken);
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result =
            topDownSession.getOutputOrRequireAndEnsureExplicitlyObserved(createTask(rootDirectory), cancelToken);
        final ArrayList<IProject> projectReferences = getProjectReferences(rootDirectory, result);
        setProjectReferences(project, projectReferences, monitor);
    }


    @Override
    public List<IProject> getDependentProjects(IBuildConfiguration buildConfiguration) throws CoreException {
        final ArrayList<IProject> dependentProjects = new ArrayList<>();
        final EclipseResourcePath rootDirectory = new EclipseResourcePath(buildConfiguration.getProject());
        logger.debug("Eclipse is asking for the dynamic dependent projects of {} on the main thread", rootDirectory);
        // NOTE: Use tryNewSession because this method is called on the main thread, and newSession may block due to
        // locking, which would cause blocking the main thread and thus the entire Eclipse UI. If another build is
        // running, we just cannot get the dependent projects :(
        Option.ofOptional(getPieComponent().getPie().tryNewSession()).ifSomeThrowing(trySession -> { // Skip if another session exists.
            try(final MixedSession session = trySession) {
                final Task<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> task = createTask(rootDirectory);
                if(session.hasBeenExecuted(task)) {
                    // NOTE: get the output of the task instead of requiring it, as requiring it may update tasks which are
                    // then not noticed by bottom-up builds. We do not have access to the changed resources here so we also
                    // cannot do a proper bottom-up build. So we just run a bottom-up build without changes and ask for the
                    // most up-to-date result.
                    final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result =
                        session.updateAffectedBy(Collections.emptySet()).getOutput(task);
                    dependentProjects.addAll(getProjectReferences(rootDirectory, result));
                }
            } catch(ExecException e) {
                final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
                exceptionPrinter.addCurrentDirectoryContext(rootDirectory);
                final String message = exceptionPrinter.printExceptionToString(e);
                logger.warn(message);
                throw new CoreException(new Status(IStatus.WARNING, SpoofaxLwbPlugin.id, IStatus.WARNING, message, null));
            } catch(InterruptedException e) {
                // Ignore
            }
        }).ifNoneThrowing(() -> {
            // Log an error if we cannot get the dynamic projects.
            final String message = "Cannot get dynamic dependent projects of " + rootDirectory + "; another PIE session is already running and we cannot block the main thread";
            logger.warn(message);
            throw new CoreException(new Status(IStatus.WARNING, SpoofaxLwbPlugin.id, IStatus.WARNING, message, null));
        });
        logger.debug("Dynamic dependent projects of {}: {}", rootDirectory, dependentProjects);
        return dependentProjects;
    }


    private Task<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> createTask(ResourcePath rootDirectory) {
        return SpoofaxLwbPlugin.getSpoofaxLwbCompilerComponent().getCfgComponent().getCfgRootDirectoryToObject().createTask(rootDirectory);
    }

    private ArrayList<IProject> getProjectReferences(EclipseResourcePath rootDirectory, Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
        final ArrayList<IProject> projectReferences = new ArrayList<>();
        result.ifOk(output -> {
            final List<Dependency> dependencies = output.compileLanguageInput.compileLanguageSpecificationInput().dependencies();
            for(Dependency dependency : dependencies) {
                if(!dependency.kinds.contains(DependencyKind.CompileTime)) continue;
                final Optional<String> relativePath = dependency.source.caseOf()
                    .path(p -> p)
                    .otherwiseEmpty();
                relativePath.ifPresent(p -> {
                    final EclipseResourcePath projectPath = rootDirectory.appendAsRelativePath(p);
                    final @Nullable IResource maybeProject = ResourcesPlugin.getWorkspace().getRoot().findMember(projectPath.getEclipsePath());
                    if(maybeProject instanceof IProject) {
                        projectReferences.add((IProject)maybeProject);
                    }
                });
            }
        });
        return projectReferences;
    }

    private void setProjectReferences(IProject project, ArrayList<IProject> projectReferences, @Nullable IProgressMonitor monitor) throws CoreException {
        logger.debug("Setting project references for {} to {}", project, projectReferences);
        final IProjectDescription projectDescription = project.getDescription();
        projectDescription.setReferencedProjects(projectReferences.toArray(new IProject[0]));
        project.setDescription(projectDescription, monitor);
    }
}
