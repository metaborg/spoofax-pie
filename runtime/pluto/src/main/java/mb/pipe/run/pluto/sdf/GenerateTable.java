package mb.pipe.run.pluto.sdf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.newsdf2table.parsetable.ParseTableGenerator;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import mb.pipe.run.spoofax.sdf.Table;

public class GenerateTable extends ABuilder<GenerateTable.Input, GenerateTable.Output> {
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

        public final Table table;


        public Output(Table table) {
            this.table = table;
        }


        public Table getPipeVal() {
            return table;
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + table.hashCode();
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
            if(!table.equals(other.table))
                return false;
            return true;
        }
    }


    public static final BuilderFactory<Input, Output, GenerateTable> factory =
        factory(GenerateTable.class, Input.class);

    public static BuildRequest<Input, Output, GenerateTable, BuilderFactory<Input, Output, GenerateTable>>
        request(Input input) {
        return request(input, GenerateTable.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, GenerateTable.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, GenerateTable.class, Input.class);
    }


    public GenerateTable(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Create parse table for SDF file " + input.mainFile;
    }

    @Override public File persistentPath(Input input) {
        return depFile("sdf2table", input.mainFile, input.includedFiles);
    }

    @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        // Read input files
        final String mainFileText = Read.build(this, new Read.Input(input.depDir, input.mainFile));
        final Map<IResource, String> texts = Maps.newHashMap();
        for(IResource file : input.includedFiles) {
            final String text = Read.build(this, new Read.Input(input.depDir, input.mainFile));
            texts.put(file, text);
        }
        texts.put(input.mainFile, mainFileText);

        // Load SDF3, required for parsing, analysis, and transformation.
        final ILanguageImpl langImpl = LoadLang.build(this, new LoadLang.Input(input.depDir, null, input.langLoc));
        final LanguageIdentifier langId = langImpl.id();

        // Parse input files
        final Map<IResource, IStrategoTerm> asts = Maps.newHashMap();
        for(Entry<IResource, String> pair : texts.entrySet()) {
            final IResource file = pair.getKey();
            final String text = pair.getValue();
            final @Nullable IStrategoTerm ast =
                Parse.build(this, new Parse.Input(input.depDir, null, langId, file, text));
            if(ast == null) {
                reportError("Unable to parse SDF file " + file + ", skipping file");
                continue;
            }
            asts.put(file, ast);
        }

        // Load project, required for analysis and transformation.
        LoadProject.build(this, new LoadProject.Input(input.depDir, null, input.specDir));

        // // Analyze
        // final Map<IResource, IStrategoTerm> analyzedAsts = Maps.newHashMap();
        // for(Entry<IResource, IStrategoTerm> pair : asts.entrySet()) {
        // final IResource file = pair.getKey();
        // final IStrategoTerm ast = pair.getValue();
        // final @Nullable IStrategoTerm analyzedAst =
        // Analyze.build(this, new Analyze.Input(input.depDir, null, langId, input.langDir, file, ast));
        // if(analyzedAst == null) {
        // reportError("Unable to analyze SDF file " + file + ", skipping file");
        // continue;
        // }
        // analyzedAsts.put(file, ast);
        // }

        // Transform
        final Origin.Builder normalizedOriginBuilder = Origin.Builder();
        final ITransformGoal transformGoal = new EndNamedGoal("to Normal Form (abstract)");
        final Map<IResource, Trans.Output> normalized = Maps.newHashMap();
        for(Entry<IResource, IStrategoTerm> pair : asts.entrySet()) {
            final IResource file = pair.getKey();
            final IStrategoTerm ast = pair.getValue();
            final Result<Trans.Output> output = Trans.requireBuild(this,
                new Trans.Input(input.depDir, null, langId, input.langLoc, file, ast, transformGoal));
            normalizedOriginBuilder.add(output.origin);
            final Trans.Output trans = output.output;
            if(trans.ast == null || trans.writtenFile == null) {
                reportError("Unable to transform SDF file " + file + ", skipping file");
                continue;
            }
            normalized.put(file, trans);
        }
        // final Origin normalizedOrigin = normalizedOriginBuilder.get();

        if(!normalized.containsKey(input.mainFile)) {
            throw new PipeRunEx("Main file " + input.mainFile + " could not be normalized");
        }
        final Trans.Output normalizedMain = normalized.get(input.mainFile);

        // Create table
        // Main input file
        final IResource mainResource = normalizedMain.writtenFile;
        final File mainFile = spoofax().resourceService.localPath(mainResource.fileObject());
        if(mainFile == null) {
            throw new PipeRunEx("Normalized main file " + mainResource + " is not on the local file system");
        }
        // Output file
        final CommonPaths spoofaxPaths = new CommonPaths(input.specDir.fileObject());
        final FileObject vfsOutputFile = spoofaxPaths.targetMetaborgDir().resolveFile("sdf-new.tbl");
        final File outputFile = spoofax().resourceService.localPath(vfsOutputFile);
        if(outputFile == null) {
            throw new PipeRunEx("Parse table output file " + vfsOutputFile + " is not on the local file system");
        }
        // Dummy output file for context grammar
        final FileObject vfsDummyfile = spoofax().resourceService.resolve("ram://ctx.grammar");
        vfsDummyfile.createFile();
        final File dummyFile = spoofax().resourceService.localFile(vfsDummyfile);
        if(dummyFile == null) {
            throw new PipeRunEx(
                "Context grammar dummy output file " + vfsDummyfile + " is not on the local file system");
        }
        // Paths
        final List<String> paths = Lists.newArrayList(spoofaxPaths.syntaxSrcGenDir().getName().getURI());
        // Create table and make dependencies
        final ParseTableGenerator generator = new ParseTableGenerator(mainFile, outputFile, dummyFile, paths, false);
        generator.createTable();
        for(File required : generator.requiredFiles()) {
            require(required);
        }
        provide(outputFile);

        return new Output(new Table(outputFile));
    }
}
