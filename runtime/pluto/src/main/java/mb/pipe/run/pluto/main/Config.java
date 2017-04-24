package mb.pipe.run.pluto.main;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.meta.core.build.LangSpecCommonPaths;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.util.Path;
import mb.pipe.run.pluto.generic.Copy;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Config extends ABuilder<Config.Input, Config.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final Path langSpecUri;
        public final Path configUri;

        public Input(File depDir, Path langSpecUri, Path configUri) {
            super(depDir);
            this.langSpecUri = langSpecUri;
            this.configUri = configUri;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final Path writtenFileUri;

        public Output(Path writtenFileUri) {
            this.writtenFileUri = writtenFileUri;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + writtenFileUri.hashCode();
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
            if(!writtenFileUri.equals(other.writtenFileUri))
                return false;
            return true;
        }
    }

    public static final BuilderFactory<Input, Output, Config> factory = factory(Config.class, Input.class);

    public static BuildRequest<Input, Output, Config, BuilderFactory<Input, Output, Config>> request(Input input) {
        return request(input, Config.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Config.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Config.class, Input.class);
    }

    public Config(Input input) {
        super(input);
    }

    @Override protected String description(Input input) {
        return "Config";
    }

    @Override public File persistentPath(Input input) {
        return depFile("config");
    }

    @Override protected Output build(Input input) throws Throwable {
        // Copy metaborg.yaml -> src-gen/metaborg.component.yaml.
        final Path from = input.langSpecUri.resolve("metaborg.yaml");
        final LangSpecCommonPaths paths = new LangSpecCommonPaths(input.langSpecUri.fileObject());
        final Path to = new Path(paths.mbComponentConfigFile());
        final Result<Copy.Output> copyConfig = Copy.requireBuild(this, new Copy.Input(input.depDir, null, from, to));
        return new Output(copyConfig.output.to);
    }
}
