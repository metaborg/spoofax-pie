package mb.pipe.run.pluto.spoofax.sdf;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.newsdf2table.parsetable.ParseTableGenerator;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.util.Path;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Sdf2Table extends ABuilder<Sdf2Table.Input, Sdf2Table.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final Path input;
        public final Path output;


        public Input(File depDir, @Nullable Origin origin, Path input, Path output) {
            super(depDir, origin);

            this.input = input;
            this.output = output;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final Path output;


        public Output(Path output) {
            this.output = output;
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + output.hashCode();
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
            if(!output.equals(other.output))
                return false;
            return true;
        }
    }


    public static final BuilderFactory<Input, Output, Sdf2Table> factory = factory(Sdf2Table.class, Input.class);

    public static BuildRequest<Input, Output, Sdf2Table, BuilderFactory<Input, Output, Sdf2Table>>
        request(Input input) {
        return request(input, Sdf2Table.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Sdf2Table.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Sdf2Table.class, Input.class);
    }


    public Sdf2Table(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Create parse table for " + input.input;
    }

    @Override public File persistentPath(Input input) {
        return depFile("sdf2table", input.input, input.output);
    }

    @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        final FileObject inputFile = input.input.fileObject();
        final File localInputFile = spoofax().resourceService.localPath(inputFile);
        final FileObject outputFile = input.output.fileObject();
        outputFile.createFile();
        final File localOutputFile = spoofax().resourceService.localPath(outputFile);

        require(localInputFile);
        final ParseTableGenerator generator =
            new ParseTableGenerator(localInputFile, localOutputFile, Lists.<String>newLinkedList(), true);
        generator.createTable();
        for(File required : generator.requiredFiles()) {
            require(required);
        }
        provide(localOutputFile);

        return new Output(input.output);
    }
}
