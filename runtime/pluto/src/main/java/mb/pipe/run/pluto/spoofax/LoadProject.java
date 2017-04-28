package mb.pipe.run.pluto.spoofax;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputTransient;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class LoadProject extends ABuilder<LoadProject.Input, OutputTransient<LoadProject.Output>> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final IResource location;


        public Input(File depDir, @Nullable Origin origin, IResource location) {
            super(depDir, origin);
            this.location = location;
        }
    }

    public static class Output implements Serializable {
        private static final long serialVersionUID = 1L;

        public transient final IProject project;


        public Output(IProject project) {
            this.project = project;
        }
    }


    public static final BuilderFactory<Input, OutputTransient<Output>, LoadProject> factory =
        factory(LoadProject.class, Input.class);

    public static
        BuildRequest<Input, OutputTransient<Output>, LoadProject, BuilderFactory<Input, OutputTransient<Output>, LoadProject>>
        request(Input input) {
        return request(input, LoadProject.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, LoadProject.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        final BuildRequest<Input, OutputTransient<Output>, LoadProject, BuilderFactory<Input, OutputTransient<Output>, LoadProject>> br =
            request(input, LoadProject.class, Input.class);
        final Origin origin = Origin.from(br);
        final Output out = requiree.requireBuild(br).val();
        return new Result<Output>(out, origin);
    }

    public static IProject build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, LoadProject.class, Input.class).output.val().project;
    }



    public LoadProject(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Load project at " + input.location;
    }

    @Override public File persistentPath(Input input) {
        return depFile("load_project", input.location);
    }

    @Override protected OutputTransient<Output> build(Input input) throws Throwable {
        final FileObject projectLocation = input.location.fileObject();
        IProject project = spoofax().projectService.get(projectLocation);
        if(project == null) {
            project = spoofax().injector.getInstance(ISimpleProjectService.class).create(projectLocation);
        }
        return OutputTransient.of(new Output(project));
    }
}
