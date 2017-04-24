package mb.pipe.run.pluto.main;

import java.io.File;
import java.io.IOException;

import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.meta.core.build.LangSpecCommonPaths;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.util.Path;
import mb.pipe.run.pluto.generic.Read;
import mb.pipe.run.pluto.spoofax.Analyze;
import mb.pipe.run.pluto.spoofax.LoadLang;
import mb.pipe.run.pluto.spoofax.LoadProject;
import mb.pipe.run.pluto.spoofax.Parse;
import mb.pipe.run.pluto.spoofax.Trans;
import mb.pipe.run.pluto.spoofax.sdf.Sdf2Table;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Sdf extends ABuilder<Sdf.Input, Sdf.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final Path langSpecUri;
        public final Path sdfLangUri;
        public final Path sdfSpecUri;


        public Input(File depDir, Path langSpecUri, Path sdfLangUri, Path sdfSpecUri) {
            super(depDir);
            this.langSpecUri = langSpecUri;
            this.sdfLangUri = sdfLangUri;
            this.sdfSpecUri = sdfSpecUri;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final Path parseTableUri;


        public Output(Path parseTableUri) {
            this.parseTableUri = parseTableUri;
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + parseTableUri.hashCode();
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
            if(!parseTableUri.equals(other.parseTableUri))
                return false;
            return true;
        }
    }


    public static final BuilderFactory<Input, Output, Sdf> factory = factory(Sdf.class, Input.class);

    public static BuildRequest<Input, Output, Sdf, BuilderFactory<Input, Output, Sdf>> request(Input input) {
        return request(input, Sdf.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Sdf.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Sdf.class, Input.class);
    }


    public Sdf(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "SDF";
    }

    @Override public File persistentPath(Input input) {
        return depFile("sdf");
    }

    @Override protected Output build(Input input) throws Throwable {
        // Read lang.sdf3.
        final Result<Read.Output> read = Read.requireBuild(this, new Read.Input(input.depDir, null, input.sdfSpecUri));

        // Load SDF3, required for parsing, analysis, and transformation.
        final Result<LoadLang.Output> lang =
            LoadLang.requireBuild(this, new LoadLang.Input(input.depDir, null, input.sdfLangUri));
        final LanguageIdentifier langId = lang.output.langImpl.id();

        // Parse lang.sdf3.
        final Result<Parse.Output> parse =
            Parse.requireBuild(this, new Parse.Input(input.depDir, null, langId, input.sdfSpecUri, read.output.text));

        // Load project, required for analysis and transformation.
        final Result<LoadProject.Output> project =
            LoadProject.requireBuild(this, new LoadProject.Input(input.depDir, null, input.langSpecUri));

        // Analyze lang.sdf3.
        final Result<Analyze.Output> analyze = Analyze.requireBuild(this,
            new Analyze.Input(input.depDir, null, langId, input.langSpecUri, input.sdfSpecUri, parse.output.ast));

        // Transform lang.sdf3 -> lang-norm.aterm.
        final Result<Trans.Output> trans = Trans.requireBuild(this, new Trans.Input(input.depDir, null, langId,
            input.langSpecUri, input.sdfSpecUri, analyze.output.ast, new EndNamedGoal("to Normal Form (abstract)")));

        // Run sdf2table on lang.norm.aterm -> target/metaborg/sdf.tbl
        final LangSpecCommonPaths paths = new LangSpecCommonPaths(project.output.project.location());
        final Result<Sdf2Table.Output> parseTable = Sdf2Table.requireBuild(this, new Sdf2Table.Input(input.depDir,
            trans.origin, trans.output.writtenFile, new Path(paths.targetMetaborgDir().resolveFile("sdf.tbl"))));

        return new Output(parseTable.output.output);
    }
}
