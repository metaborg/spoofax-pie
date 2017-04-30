package mb.pipe.run.pluto.esv;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.model.parse.IToken;
import mb.pipe.run.core.model.style.IStyling;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;
import mb.pipe.run.spoofax.esv.Styler;
import mb.pipe.run.spoofax.esv.StylingRules;

public class Style extends ABuilder<Style.Input, Style.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final Collection<IToken> tokenStream;
        public final StylingRules stylingRules;


        public Input(File depDir, @Nullable Origin origin, Collection<IToken> tokenStream, StylingRules stylingRules) {
            super(depDir, origin);

            this.tokenStream = tokenStream;
            this.stylingRules = stylingRules;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final IStyling styling;


        public Output(IStyling styling) {
            this.styling = styling;
        }


        public IStyling getPipeVal() {
            return styling;
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + styling.hashCode();
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
            if(!styling.equals(other.styling))
                return false;
            return true;
        }
    }


    public static final BuilderFactory<Input, Output, Style> factory = factory(Style.class, Input.class);

    public static BuildRequest<Input, Output, Style, BuilderFactory<Input, Output, Style>> request(Input input) {
        return request(input, Style.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Style.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Style.class, Input.class);
    }

    public static @Nullable IStyling build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Style.class, Input.class).output.styling;
    }


    public Style(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Parse";
    }

    @Override public File persistentPath(Input input) {
        return depFile("style", input.tokenStream.hashCode(), input.stylingRules.hashCode());
    }

    @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        final Styler styler = new Styler(input.stylingRules);
        final IStyling styling = styler.style(input.tokenStream);

        return new Output(styling);
    }
}
