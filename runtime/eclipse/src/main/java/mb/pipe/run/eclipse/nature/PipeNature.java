package mb.pipe.run.eclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.google.inject.Injector;

import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.build.PipeProjectBuilder;
import mb.pipe.run.eclipse.util.BuilderUtils;
import mb.pipe.run.eclipse.vfs.EclipsePathSrv;
import mb.vfs.path.PPath;

public class PipeNature implements IProjectNature {
    public static final String id = PipePlugin.id + ".nature";

    private final EclipsePathSrv pathSrv;
    private final BuilderUtils builderUtils;

    private IProject project;


    public PipeNature() {
        final Injector injector = PipePlugin.spoofaxFacade().injector;
        this.builderUtils = injector.getInstance(BuilderUtils.class);
        this.pathSrv = injector.getInstance(EclipsePathSrv.class);
    }


    @Override public void configure() throws CoreException {
        builderUtils.prepend(PipeProjectBuilder.id, project, null);
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
    }

    @Override public void deconfigure() throws CoreException {
        builderUtils.removeFrom(PipeProjectBuilder.id, project, null);
    }

    @Override public IProject getProject() {
        return project;
    }

    @Override public void setProject(IProject project) {
        this.project = project;
    }
}
