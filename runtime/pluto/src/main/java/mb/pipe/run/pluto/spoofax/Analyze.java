package mb.pipe.run.pluto.spoofax;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Analyze extends ABuilder<Analyze.Input, Analyze.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final LanguageIdentifier langId;
        public final IResource project;
        public final IResource file;
        public final @Nullable IStrategoTerm ast;


        public Input(File depDir, @Nullable Origin origin, LanguageIdentifier langId, IResource project, IResource file,
            @Nullable IStrategoTerm ast) {
            super(depDir, origin);

            this.langId = langId;
            this.project = project;
            this.file = file;
            this.ast = ast;
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


    public static final BuilderFactory<Input, Output, Analyze> factory = factory(Analyze.class, Input.class);

    public static BuildRequest<Input, Output, Analyze, BuilderFactory<Input, Output, Analyze>> request(Input input) {
        return request(input, Analyze.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Analyze.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Analyze.class, Input.class);
    }

    public static @Nullable IStrategoTerm build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Analyze.class, Input.class).output.ast;
    }


    public Analyze(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Analyze AST of " + input.langId;
    }

    @Override public File persistentPath(Input input) {
        return depFile("analyze", input.langId, input.project, input.file, input.ast);
    }

    @Override protected Analyze.Output build(Input input) throws Throwable {
        if(input.ast == null) {
            return new Output(null);
        }

        requireOrigins();

        final ILanguageImpl langImpl = spoofax().languageService.getImpl(input.langId);
        final FileObject resource = input.file.fileObject();
        final IProject project = spoofax().projectService.get(resource);
        if(project == null) {
            throw new MetaborgException("Cannot analyze " + resource + ", it does not belong to a project");
        }

        final ISpoofaxInputUnit inputUnit = spoofax().unitService.inputUnit(resource, "hack", langImpl, null);
        final ISpoofaxParseUnit parseUnit =
            spoofax().unitService.parseUnit(inputUnit, new ParseContrib(true, true, input.ast, Iterables2.empty(), -1));

        final IContext spoofaxContext = spoofax().contextService.get(project.location(), project, langImpl);
        try(IClosableLock lock = spoofaxContext.write()) {
            final ISpoofaxAnalyzeResult analyzeResult = spoofax().analysisService.analyze(parseUnit, spoofaxContext);
            return new Output(analyzeResult.result().ast());
        }
    }
}
