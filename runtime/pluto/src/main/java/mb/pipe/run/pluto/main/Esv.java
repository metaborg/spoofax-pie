package mb.pipe.run.pluto.main;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.language.LanguageIdentifier;
import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.util.Path;
import mb.pipe.run.pluto.generic.Read;
import mb.pipe.run.pluto.spoofax.LoadLang;
import mb.pipe.run.pluto.spoofax.LoadProject;
import mb.pipe.run.pluto.spoofax.Parse;
import mb.pipe.run.pluto.spoofax.Trans;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Esv extends ABuilder<Esv.Input, Esv.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final Path langSpecUri;
        public final Path esvLangUri;
        public final Path esvSpecUri;

        public Input(File depDir, Path langSpecUri, Path esvLangUri, Path esvSpecUri) {
            super(depDir);
            this.langSpecUri = langSpecUri;
            this.esvLangUri = esvLangUri;
            this.esvSpecUri = esvSpecUri;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final @Nullable IStrategoTerm term;
        public final @Nullable Path writtenFileUri;

        public Output(@Nullable IStrategoTerm term, @Nullable Path writtenFileUri) {
            this.term = term;
            this.writtenFileUri = writtenFileUri;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((term == null) ? 0 : term.hashCode());
            result = prime * result + ((writtenFileUri == null) ? 0 : writtenFileUri.hashCode());
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
            if(term == null) {
                if(other.term != null)
                    return false;
            } else if(!term.equals(other.term))
                return false;
            if(writtenFileUri == null) {
                if(other.writtenFileUri != null)
                    return false;
            } else if(!writtenFileUri.equals(other.writtenFileUri))
                return false;
            return true;
        }
    }

    public static final BuilderFactory<Input, Output, Esv> factory = factory(Esv.class, Input.class);

    public static BuildRequest<Input, Output, Esv, BuilderFactory<Input, Output, Esv>> request(Input input) {
        return request(input, Esv.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Esv.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Esv.class, Input.class);
    }

    public Esv(Input input) {
        super(input);
    }

    @Override protected String description(Input input) {
        return "ESV";
    }

    @Override public File persistentPath(Input input) {
        return depFile("esv");
    }

    @Override protected Output build(Input input) throws Throwable {
        // Read Main.esv.
        final Result<Read.Output> read = Read.requireBuild(this, new Read.Input(input.depDir, null, input.esvSpecUri));

        // Load ESV, required for parsing and transformation.
        final Result<LoadLang.Output> lang =
            LoadLang.requireBuild(this, new LoadLang.Input(input.depDir, null, input.esvLangUri));
        final LanguageIdentifier langId = lang.output.langImpl.id();

        // Parse Main.esv.
        final Result<Parse.Output> parse =
            Parse.requireBuild(this, new Parse.Input(input.depDir, null, langId, input.esvSpecUri, read.output.text));

        // Load project, required for transformation.
        final Result<LoadProject.Output> project =
            LoadProject.requireBuild(this, new LoadProject.Input(input.depDir, null, input.langSpecUri));

        // Transform Main.esv -> target/metaborg/editor.esv.af.
        final Result<Trans.Output> trans = Trans.requireBuild(this, new Trans.Input(input.depDir, null, langId,
            input.langSpecUri, input.esvSpecUri, parse.output.ast, new CompileGoal()));

        return new Output(trans.output.ast, trans.output.writtenFile);
    }
}
