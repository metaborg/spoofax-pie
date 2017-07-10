package mb.pipe.run.eclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.google.inject.Injector;

import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.build.PipeProjectBuilder;
import mb.pipe.run.eclipse.build.Projects;
import mb.pipe.run.eclipse.util.BuilderUtils;

public class PipeNature implements IProjectNature {
    public static final String id = PipePlugin.id + ".nature";

    private final BuilderUtils builderUtils;
    private final Projects projects;

    private IProject project;


    public PipeNature() {
        final Injector injector = PipePlugin.pipeFacade().injector;
        this.builderUtils = injector.getInstance(BuilderUtils.class);
        this.projects = injector.getInstance(Projects.class);
    }


    @Override public void configure() throws CoreException {
        builderUtils.prepend(PipeProjectBuilder.id, project, null);
        projects.addProject(project);
    }

    @Override public void deconfigure() throws CoreException {
        builderUtils.removeFrom(PipeProjectBuilder.id, project, null);
        projects.removeProject(project);
    }

    @Override public IProject getProject() {
        return project;
    }

    @Override public void setProject(IProject project) {
        this.project = project;
    }
}
