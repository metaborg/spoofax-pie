package mb.pipe.run.pluto.vfs;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import build.pluto.stamp.FileHashStamper;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Read extends ABuilder<Read.Input, Read.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final IResource file;


        public Input(IContext context, @Nullable Origin origin, IResource file) {
            super(context, origin);

            this.file = file;
        }

        public Input(IContext context, IResource file) {
            this(context, null, file);
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final String text;


        public Output(String text) {
            this.text = text;
        }


        public String getPipeVal() {
            return text;
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

    public static String build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Read.class, Input.class).output.text;
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
        require(toFile(input.file), FileHashStamper.instance);
        final String text = spoofax().sourceTextService.text(input.file.fileObject());
        return new Output(text);
    }
}
