package mb.spoofax.lwb.eclipse;

import mb.cfg.Dependency;
import mb.cfg.DependencyKind;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.log.api.Logger;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.dagger.PieComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IDynamicReferenceProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SpoofaxLwbDynamicReferenceProvider implements IDynamicReferenceProvider {
    private final Logger logger;

    public SpoofaxLwbDynamicReferenceProvider() {
        this.logger = SpoofaxPlugin.getLoggerComponent().getLoggerFactory().create(getClass());
    }

    @Override public List<IProject> getDependentProjects(IBuildConfiguration buildConfiguration) throws CoreException {
        final ArrayList<IProject> dependentProjects = new ArrayList<>();
        final EclipseResourcePath rootDirectory = new EclipseResourcePath(buildConfiguration.getProject());
        logger.debug("Eclipse is asking for the dependent projects of {}", rootDirectory);
        final PieComponent pieComponent = getPieComponent();
        try(final MixedSession session = pieComponent.getPie().newSession()) {
            final Task<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> task =
                SpoofaxLwbPlugin.getSpoofaxLwbCompilerComponent().getCfgComponent().getCfgRootDirectoryToObject().createTask(rootDirectory);
            if(session.hasBeenExecuted(task)) {
                // NOTE: get the output of the task instead of requiring it, as requiring it may update tasks which are
                // then not noticed by bottom-up builds. We do not have access to the changed resources here so we also
                // cannot do a proper bottom-up build. So we just run a bottom-up build without changes and ask for the
                // most up-to-date result.
                final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result =
                    session.updateAffectedBy(Collections.emptySet()).getOutput(task);
                final List<Dependency> dependencies = result.unwrap().compileLanguageInput.compileLanguageSpecificationInput().dependencies();
                for(Dependency dependency : dependencies) {
                    if(!dependency.kinds.contains(DependencyKind.CompileTime)) continue;
                    final Optional<String> relativePath = dependency.source.caseOf()
                        .path(p -> p)
                        .otherwiseEmpty();
                    relativePath.ifPresent(p -> {
                        final EclipseResourcePath projectPath = rootDirectory.appendAsRelativePath(p);
                        final @Nullable IResource project = ResourcesPlugin.getWorkspace().getRoot().findMember(projectPath.getEclipsePath());
                        if(project instanceof IProject) {
                            dependentProjects.add((IProject)project);
                        }
                    });
                }
            }
        } catch(ExecException | CfgRootDirectoryToObjectException e) {
            final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
            exceptionPrinter.addCurrentDirectoryContext(rootDirectory);
            final String message = exceptionPrinter.printExceptionToString(e);
            throw new CoreException(new Status(IStatus.ERROR, SpoofaxLwbPlugin.id, IStatus.ERROR, message, null));
        } catch(InterruptedException e) {
            // Ignore
        }
        logger.debug("Dependent projects of {}: {}", rootDirectory, dependentProjects);
        return dependentProjects;
    }

    private PieComponent getPieComponent() {
        return SpoofaxPlugin.getStaticComponentManager().getComponentGroup("mb.spoofax.lwb").unwrap().getPieComponent();
    }
}
