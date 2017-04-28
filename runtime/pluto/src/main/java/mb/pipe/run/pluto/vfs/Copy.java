package mb.pipe.run.pluto.vfs;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;
import mb.pipe.run.pluto.vfs.Read.Input;

public class Copy extends ABuilder<Copy.Input, Copy.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final IResource from;
        public final IResource to;


        public Input(File depDir, @Nullable Origin origin, IResource from, IResource to) {
            super(depDir, origin);

            this.from = from;
            this.to = to;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final IResource to;


        public Output(IResource to) {
            this.to = to;
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + to.hashCode();
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            final Output other = (Output) obj;
            if(!to.equals(other.to))
                return false;
            return true;
        }
    }


    public static final BuilderFactory<Input, Output, Copy> factory = factory(Copy.class, Input.class);

    public static BuildRequest<Input, Output, Copy, BuilderFactory<Input, Output, Copy>> request(Input input) {
        return request(input, Copy.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Copy.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Copy.class, Input.class);
    }
    
    public static void build(Builder<?, ?> requiree, Input input) throws IOException {
        requireBuild(requiree, input, Copy.class, Input.class);
    }


    public Copy(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Copy " + input.from + " to " + input.to;
    }

    @Override public File persistentPath(Input input) {
        return depFile("copy", input.from, input.to);
    }

    @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        final FileObject inputResource = input.from.fileObject();
        require(inputResource);
        final FileObject outputResource = input.to.fileObject();
        outputResource.copyFrom(inputResource, new AllFileSelector());
        provide(outputResource);
        return new Output(input.to);
    }
}
