package mb.pipe.run.pluto.generic;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import build.pluto.stamp.FileHashStamper;
import mb.pipe.run.core.util.Path;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Read extends ABuilder<Read.Input, Read.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final Path file;


        public Input(File depDir, @Nullable Origin origin, Path file) {
            super(depDir, origin);

            this.file = file;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final String text;


        public Output(String text) {
            this.text = text;
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + text.hashCode();
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
            if(!text.equals(other.text))
                return false;
            return true;
        }
    }


    public static final BuilderFactory<Input, Output, Read> factory = factory(Read.class, Input.class);

    public static BuildRequest<Input, Output, Read, BuilderFactory<Input, Output, Read>> request(Input input) {
        return request(input, Read.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Read.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Read.class, Input.class);
    }


    public Read(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Read text from " + input.file;
    }

    @Override public File persistentPath(Input input) {
        return depFile("read", input.file);
    }

    @Override protected Output build(Input input) throws Throwable {
        final FileObject resource = input.file.fileObject();
        require(toFile(resource), FileHashStamper.instance);
        final String text = spoofax().sourceTextService.text(resource);
        return new Output(text);
    }
}
