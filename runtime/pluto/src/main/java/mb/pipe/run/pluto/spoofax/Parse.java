package mb.pipe.run.pluto.spoofax;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Parse extends ABuilder<Parse.Input, Parse.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final LanguageIdentifier langId;
        public final IResource file;
        public final String text;


        public Input(IContext context, @Nullable Origin origin, LanguageIdentifier langId, IResource file, String text) {
            super(context, origin);

            this.langId = langId;
            this.file = file;
            this.text = text;
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
            Output other = (Output) obj;
            if(ast == null) {
                if(other.ast != null)
                    return false;
            } else if(!ast.equals(other.ast))
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
        return "Parse text of " + input.langId;
    }

    @Override public File persistentPath(Input input) {
        return depFile("parse", input.langId, input.file, input.text);
    }

    @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        final ILanguageImpl langImpl = spoofax().languageService.getImpl(input.langId);
        final SyntaxFacet facet = langImpl.facet(SyntaxFacet.class);
        if(facet != null) {
            final FileObject parseTableFile = facet.parseTable;
            if(parseTableFile != null) {
                final File localFile = spoofax().resourceService.localPath(parseTableFile);
                if(localFile != null) {
                    require(localFile);
                }
            }
        }
        final FileObject resource = input.file.fileObject();
        final ISpoofaxInputUnit inputUnit = spoofax().unitService.inputUnit(resource, input.text, langImpl, null);
        final ISpoofaxParseUnit parseUnit = spoofax().syntaxService.parse(inputUnit);
        return new Output(parseUnit.ast());
    }
}
