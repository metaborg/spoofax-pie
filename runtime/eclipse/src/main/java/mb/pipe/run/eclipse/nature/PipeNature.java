package mb.pipe.run.eclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.build.PipeProjectBuilder;
import mb.pipe.run.eclipse.util.BuilderUtils;

public class PipeNature implements IProjectNature {
    public static final String id = PipePlugin.id + ".nature";

    private final BuilderUtils builderUtils;

    private IProject project;


    public PipeNature() {
        this.builderUtils = PipePlugin.pipeFacade().injector.getInstance(BuilderUtils.class);
    }


    @Override public void configure() throws CoreException {
        builderUtils.prepend(PipeProjectBuilder.id, project, null);
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
