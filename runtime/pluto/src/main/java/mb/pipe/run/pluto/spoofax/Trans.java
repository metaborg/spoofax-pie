package mb.pipe.run.pluto.spoofax;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformOutput;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Iterables;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.core.vfs.VFSResource;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

public class Trans extends ABuilder<Trans.Input, Trans.Output> {
    public static class Input extends AInput {
        private static final long serialVersionUID = 1L;

        public final LanguageIdentifier langId;
        public final IResource project;
        public final IResource file;
        public final @Nullable IStrategoTerm ast;
        public final ITransformGoal goal;


        public Input(File depDir, @Nullable Origin origin, LanguageIdentifier langId, IResource project, IResource file,
            @Nullable IStrategoTerm ast, ITransformGoal goal) {
            super(depDir, origin);

            this.langId = langId;
            this.project = project;
            this.file = file;
            this.ast = ast;
            this.goal = goal;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 1L;

        public final @Nullable IStrategoTerm ast;
        public final @Nullable IResource writtenFile;


        public Output(@Nullable IStrategoTerm ast, @Nullable IResource writtenFile) {
            this.ast = ast;
            this.writtenFile = writtenFile;
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ast == null) ? 0 : ast.hashCode());
            result = prime * result + ((writtenFile == null) ? 0 : writtenFile.hashCode());
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
            if(writtenFile == null) {
                if(other.writtenFile != null)
                    return false;
            } else if(!writtenFile.equals(other.writtenFile))
                return false;
            return true;
        }
    }


    public static final BuilderFactory<Input, Output, Trans> factory = factory(Trans.class, Input.class);

    public static BuildRequest<Input, Output, Trans, BuilderFactory<Input, Output, Trans>> request(Input input) {
        return request(input, Trans.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, Trans.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Trans.class, Input.class);
    }

    public static Output build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, Trans.class, Input.class).output;
    }

    
    public Trans(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Transform AST of " + input.langId + " with goal " + input.goal;
    }

    @Override public File persistentPath(Input input) {
        return depFile("transform", input.langId, input.project, input.file, input.ast, input.goal);
    }

    @Override protected Output build(Input input) throws Throwable {
        if(input.ast == null) {
            return new Output(null, null);
        }

        requireOrigins();

        final ILanguageImpl langImpl = spoofax().languageService.getImpl(input.langId);
        final FileObject resource = input.file.fileObject();
        final IProject project = spoofax().projectService.get(resource);
        if(project == null) {
            throw new MetaborgException("Cannot transform " + resource + ", it does not belong to a project");
        }

        final ISpoofaxInputUnit inputUnit = spoofax().unitService.inputUnit(resource, "hack", langImpl, null);
        final ISpoofaxParseUnit parseUnit =
            spoofax().unitService.parseUnit(inputUnit, new ParseContrib(true, true, input.ast, Iterables2.empty(), -1));
        final IContext spoofaxContext = spoofax().contextService.get(project.location(), project, langImpl);
        final ISpoofaxAnalyzeUnit analyzeUnit = spoofax().unitService.analyzeUnit(parseUnit,
            new AnalyzeContrib(true, true, true, input.ast, Iterables2.empty(), -1), spoofaxContext);

        try(IClosableLock lock = spoofaxContext.read()) {
            final Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> result =
                spoofax().transformService.transform(analyzeUnit, spoofaxContext, input.goal);
            final ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> unit = result.iterator().next();
            final IStrategoTerm ast = unit.ast();
            final ISpoofaxTransformOutput output = Iterables.get(unit.outputs(), 0);
            final FileObject outputResource = output.output();
            final @Nullable IResource writtenFile;
            if(outputResource != null) {
                provide(outputResource);
                writtenFile = new VFSResource(outputResource);
            } else {
                writtenFile = null;
            }

            return new Output(ast, writtenFile);
        }
    }
}
