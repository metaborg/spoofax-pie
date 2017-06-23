package mb.pipe.run.eclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.build.PipeProgramProjectBuilder;
import mb.pipe.run.eclipse.util.BuilderUtils;

public class PipeProgramNature implements IProjectNature {
    public static final String id = PipePlugin.id + ".nature.program";

    private final BuilderUtils builderUtils;

    private IProject project;


    public PipeProgramNature() {
        this.builderUtils = PipePlugin.pipeFacade().injector.getInstance(BuilderUtils.class);
    }


    @Override public void configure() throws CoreException {
        builderUtils.prepend(PipeProgramProjectBuilder.id, project, null);
    }

    @Override public void deconfigure() throws CoreException {
        builderUtils.removeFrom(PipeProgramProjectBuilder.id, project, null);
    }

    @Override public IProject getProject() {
        return project;
    }

    @Override public void setProject(IProject project) {
        this.project = project;
    }
}
