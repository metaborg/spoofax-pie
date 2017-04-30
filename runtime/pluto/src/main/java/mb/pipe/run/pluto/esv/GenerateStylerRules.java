package mb.pipe.run.pluto.esv;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.PipeRunEx;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.pluto.spoofax.LoadLang;
import mb.pipe.run.pluto.spoofax.LoadProject;
import mb.pipe.run.pluto.spoofax.Parse;
import mb.pipe.run.pluto.spoofax.Trans;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;
import mb.pipe.run.pluto.vfs.Read;
import mb.pipe.run.spoofax.esv.StylingRules;
import mb.pipe.run.spoofax.esv.StylingRulesFromESV;

public class GenerateStylerRules extends ABuilder<GenerateStylerRules.Input, GenerateStylerRules.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final IResource langLoc;
        public final IResource specDir;
        public final IResource mainFile;
        public final Collection<IResource> includedFiles;


        public Input(File depDir, @Nullable Origin origin, IResource langLoc, IResource specDir, IResource mainFile,
            Collection<IResource> includedFiles) {
            super(depDir, origin);

            this.langLoc = langLoc;
            this.specDir = specDir;
            this.mainFile = mainFile;
            this.includedFiles = includedFiles;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final StylingRules stylingRules;


        public Output(StylingRules stylingRules) {
            this.stylingRules = stylingRules;
        }


        public StylingRules getPipeVal() {
            return stylingRules;
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + stylingRules.hashCode();
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
            if(!stylingRules.equals(other.stylingRules))
                return false;
            return true;
        }
    }


    public static final BuilderFactory<Input, Output, GenerateStylerRules> factory =
        factory(GenerateStylerRules.class, Input.class);

    public static BuildRequest<Input, Output, GenerateStylerRules, BuilderFactory<Input, Output, GenerateStylerRules>>
        request(Input input) {
        return request(input, GenerateStylerRules.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, GenerateStylerRules.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, GenerateStylerRules.class, Input.class);
    }


    public GenerateStylerRules(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "ESV";
    }

    @Override public File persistentPath(Input input) {
        return depFile("esv");
    }

    @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        // Read input file
        final String text = Read.build(this, new Read.Input(input.depDir, input.mainFile));

        // Create dependencies to included files
        for(IResource includedFile : input.includedFiles) {
            require(includedFile.fileObject());
        }

        // Load ESV, required for parsing, analysis, and transformation.
        final ILanguageImpl langImpl = LoadLang.build(this, new LoadLang.Input(input.depDir, null, input.langLoc));
        final LanguageIdentifier langId = langImpl.id();

        // Parse input file
        final @Nullable IStrategoTerm ast =
            Parse.build(this, new Parse.Input(input.depDir, null, langId, input.mainFile, text));

        if(ast == null) {
            throw new PipeRunEx("Main ESV file " + input.mainFile + " could not be parsed");
        }

        // Load project, required for analysis and transformation.
        LoadProject.build(this, new LoadProject.Input(input.depDir, null, input.specDir));

        // Transform
        final Trans.Output output = Trans.build(this,
            new Trans.Input(input.depDir, null, langId, input.langLoc, input.mainFile, ast, new CompileGoal()));

        if(output.ast == null) {
            throw new PipeRunEx("Main ESV file " + input.mainFile + " could not be compiled");
        }

        final StylingRules rules =
            pipe().injector.getInstance(StylingRulesFromESV.class).create((IStrategoAppl) output.ast);

        return new Output(rules);
    }
}
