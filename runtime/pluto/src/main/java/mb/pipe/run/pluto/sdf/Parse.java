package mb.pipe.run.pluto.sdf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.core.model.message.IMsg;
import mb.pipe.run.core.model.parse.IToken;
import mb.pipe.run.core.util.ITuple;
import mb.pipe.run.core.util.Tuple;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;
import mb.pipe.run.spoofax.sdf.ParseOutput;
import mb.pipe.run.spoofax.sdf.Parser;
import mb.pipe.run.spoofax.sdf.Table;

public class Parse extends ABuilder<Parse.Input, Parse.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final String text;
        public final String startSymbol;
        public final Table table;


        public Input(IContext context, @Nullable Origin origin, String text, String startSymbol, Table table) {
            super(context, origin);

            this.text = text;
            this.startSymbol = startSymbol;
            this.table = table;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final @Nullable IStrategoTerm ast;
        public final @Nullable Collection<IToken> tokenStream;
        public final Collection<IMsg> messages;


        public Output(@Nullable IStrategoTerm ast, @Nullable Collection<IToken> tokenStream,
            Collection<IMsg> messages) {
            this.ast = ast;
            this.tokenStream = tokenStream;
            this.messages = messages;
        }


        public ITuple getPipeVal() {
            return new Tuple(ast, tokenStream, messages);
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ast == null) ? 0 : ast.hashCode());
            result = prime * result + ((tokenStream == null) ? 0 : tokenStream.hashCode());
            result = prime * result + messages.hashCode();
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
            if(tokenStream == null) {
                if(other.tokenStream != null)
                    return false;
            } else if(!tokenStream.equals(other.tokenStream))
                return false;
            if(!messages.equals(other.messages))
                return false;
            return true;
        }
    }


    public static final BuilderFactory<Input, Output, Parse> factory = factory(Parse.class, Input.class);

    public static BuildRequest<Input, Output, Parse, BuilderFactory<Input, Output, Parse>> request(Input input) {
        return request(input, Parse.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Parse.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Parse.class, Input.class);
    }

    public static @Nullable IStrategoTerm build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Parse.class, Input.class).output.ast;
    }


    public Parse(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Parse";
    }

    @Override public File persistentPath(Input input) {
        return depFile("parse", input.text, input.table);
    }

    @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        final Parser parser = input.table.createParser(new TermFactory());
        final ParseOutput output = parser.parse(input.text, input.startSymbol);

        return new Output(output.ast, output.tokenStream, output.messages);
    }
}
