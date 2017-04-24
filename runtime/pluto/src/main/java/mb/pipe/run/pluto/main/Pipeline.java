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
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Pipeline extends ABuilder<Pipeline.Input, Pipeline.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final Path langSpecUri;

        public final Path configUri;

        public final Path esvLangUri;
        public final Path esvSpecUri;

        public final Path sdfLangUri;
        public final Path sdfSpecUri;

        public final Path fileToParseUri;

        public Input(File depDir, @Nullable Origin origin, Path langSpecUri, Path configUri, Path esvLangUri,
            Path esvSpecUri, Path sdfLangUri, Path sdfSpecUri, Path fileToParseUri) {
            super(depDir, origin);
            this.langSpecUri = langSpecUri;
            this.configUri = configUri;
            this.esvLangUri = esvLangUri;
            this.esvSpecUri = esvSpecUri;
            this.sdfLangUri = sdfLangUri;
            this.sdfSpecUri = sdfSpecUri;
            this.fileToParseUri = fileToParseUri;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final @Nullable IStrategoTerm ast;

        public Output(@Nullable IStrategoTerm ast) {
            this.ast = ast;
        }
    }

    public static final BuilderFactory<Input, Output, Pipeline> factory = factory(Pipeline.class, Input.class);

    public static BuildRequest<Input, Output, Pipeline, BuilderFactory<Input, Output, Pipeline>> request(Input input) {
        return request(input, Pipeline.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Pipeline.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Pipeline.class, Input.class);
    }

    public Pipeline(Input input) {
        super(input);
    }

    @Override protected String description(Input input) {
        return "Pipeline";
    }

    @Override public File persistentPath(Input input) {
        return depFile("pipeline");
    }

    @Override protected Output build(Input input) throws Throwable {
        // Build the language specification into a language product.
        final Result<Config.Output> config =
            Config.requireBuild(this, new Config.Input(input.depDir, input.langSpecUri, input.configUri));
        final Result<Esv.Output> esv =
            Esv.requireBuild(this, new Esv.Input(input.depDir, input.langSpecUri, input.esvLangUri, input.esvSpecUri));
        final Result<Sdf.Output> sdf =
            Sdf.requireBuild(this, new Sdf.Input(input.depDir, input.langSpecUri, input.sdfLangUri, input.sdfSpecUri));

        // Parse a file using the built language product.
        final Origin origin = Origin.Builder().add(config.origin).add(esv.origin).add(sdf.origin).get();
        final Result<Lang.Output> lang =
            Lang.requireBuild(this, new Lang.Input(input.depDir, origin, input.langSpecUri, input.fileToParseUri));

        return new Output(lang.output.ast);
    }
}
