package mb.spoofax.runtime.eclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.google.inject.Injector;

import mb.log.Logger;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.build.SpoofaxProjectBuilder;
import mb.spoofax.runtime.eclipse.pipeline.PipelineAdapter;
import mb.spoofax.runtime.eclipse.util.BuilderUtils;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.vfs.path.PPath;

public class SpoofaxNature implements IProjectNature {
    public static final String id = SpoofaxPlugin.id + ".nature";

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final BuilderUtils builderUtils;
    private final PipelineAdapter pipelineAdapter;

    private IProject project;


    public SpoofaxNature() {
        final Injector injector = SpoofaxPlugin.spoofaxFacade().injector;
        this.logger = injector.getInstance(Logger.class).forContext(getClass());
        this.pathSrv = injector.getInstance(EclipsePathSrv.class);
        this.builderUtils = injector.getInstance(BuilderUtils.class);
        this.pipelineAdapter = injector.getInstance(PipelineAdapter.class);
    }


    @Override public void configure() throws CoreException {
        builderUtils.prepend(SpoofaxProjectBuilder.id, project, null);
        final PPath root = pathSrv.resolveWorkspaceRoot();
        final PPath rootProjectPath = root.resolve("root");
        final IResource rootResource = pathSrv.unresolve(rootProjectPath);
        if(rootResource != null && rootResource instanceof IProject) {
            final IProject rootProject = (IProject) rootResource;
            if(!project.equals(rootProject)) {
                final IProjectDescription projectDesc = project.getDescription();
                projectDesc.setReferencedProjects(new IProject[] { rootProject });
                project.setDescription(projectDesc, null);
            }
        }
        logger.debug("Spoofax nature was added to project {}", project);
        pipelineAdapter.addProject(project);
    }

    @Override public void deconfigure() throws CoreException {
        builderUtils.removeFrom(SpoofaxProjectBuilder.id, project, null);
        logger.debug("Spoofax nature was removed from project {}", project);
        pipelineAdapter.removeProject(project);
    }

    @Override public IProject getProject() {
        return project;
    }

    @Override public void setProject(IProject project) {
        this.project = project;
    }
}
