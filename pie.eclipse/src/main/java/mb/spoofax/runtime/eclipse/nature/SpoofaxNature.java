package mb.spoofax.runtime.eclipse.nature;

import com.google.inject.Injector;
import mb.fs.java.JavaFSNode;
import mb.log.api.Logger;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.build.SpoofaxProjectBuilder;
import mb.spoofax.runtime.eclipse.pipeline.PipelineAdapter;
import mb.spoofax.runtime.eclipse.util.BuilderUtils;
import mb.spoofax.runtime.eclipse.util.FileUtils;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

public class SpoofaxNature implements IProjectNature {
    public static final String id = SpoofaxPlugin.id + ".nature";

    private final Logger logger;
    private final FileUtils pathSrv;
    private final BuilderUtils builderUtils;
    private final PipelineAdapter pipelineAdapter;

    private IProject project;


    public SpoofaxNature() {
        final Injector injector = SpoofaxPlugin.spoofaxFacade().injector;
        this.logger = injector.getInstance(Logger.class).forContext(getClass());
        this.pathSrv = injector.getInstance(FileUtils.class);
        this.builderUtils = injector.getInstance(BuilderUtils.class);
        this.pipelineAdapter = injector.getInstance(PipelineAdapter.class);
    }


    @Override public void configure() throws CoreException {
        builderUtils.prepend(SpoofaxProjectBuilder.id, project, null);
        final JavaFSNode rootNode = pathSrv.workspaceRootNode();
        final JavaFSNode rootProjectNode = rootNode.appendSegment("root");
        final IResource rootResource = pathSrv.toResource(rootProjectNode);
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
