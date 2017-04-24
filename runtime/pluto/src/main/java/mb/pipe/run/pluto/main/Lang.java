package mb.pipe.run.pluto.main;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.util.Path;
import mb.pipe.run.pluto.generic.Read;
import mb.pipe.run.pluto.spoofax.LoadLang;
import mb.pipe.run.pluto.spoofax.Parse;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Lang extends ABuilder<Lang.Input, Lang.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final Path langSpecUri;
        public final Path fileToParseUri;

        public Input(File depDir, @Nullable Origin origin, Path langSpecUri, Path fileToParseUri) {
            super(depDir, origin);
            this.langSpecUri = langSpecUri;
            this.fileToParseUri = fileToParseUri;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final @Nullable IStrategoTerm ast;

        public Output(@Nullable IStrategoTerm ast) {
            this.ast = ast;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ast == null) ? 0 : ast.hashCode());
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
            if(ast == null) {
                if(other.ast != null)
                    return false;
            } else if(!ast.equals(other.ast))
                return false;
            return true;
        }
    }

    public static final BuilderFactory<Input, Output, Lang> factory = factory(Lang.class, Input.class);

    public static BuildRequest<Input, Output, Lang, BuilderFactory<Input, Output, Lang>> request(Input input) {
        return request(input, Lang.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Lang.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Lang.class, Input.class);
    }

    public Lang(Input input) {
        super(input);
    }

    @Override protected String description(Input input) {
        return "Lang";
    }

    @Override public File persistentPath(Input input) {
        return depFile("lang");
    }

    @Override protected Output build(Input input) throws Throwable {
        // Read test.min.
        final Result<Read.Output> read =
            Read.requireBuild(this, new Read.Input(input.depDir, null, input.fileToParseUri));

        // Load target language
        final Result<LoadLang.Output> lang =
            LoadLang.requireBuild(this, new LoadLang.Input(input.depDir, input.origin, input.langSpecUri));

        // Parse test.min.
        final Result<Parse.Output> parse = Parse.requireBuild(this, new Parse.Input(input.depDir, input.origin,
            lang.output.langImpl.id(), input.fileToParseUri, read.output.text));

        return new Output(parse.output.ast);
    }
}
