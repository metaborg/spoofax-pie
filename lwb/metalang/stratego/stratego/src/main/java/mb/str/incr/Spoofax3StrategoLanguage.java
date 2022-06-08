package mb.str.incr;

import mb.common.result.Result;
import mb.common.util.IOUtil;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseInput;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.str.StrategoParser;
import mb.str.StrategoParserSelector;
import mb.str.StrategoScope;
import mb.str.task.spoofax.StrategoParseWrapper;
import mb.stratego.build.strincr.StrategoLanguage;
import mb.stratego.build.strincr.data.GTEnvironment;
import mb.stratego.build.termvisitors.DisambiguateAsAnno;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.HybridInterpreter;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

@StrategoScope
public class Spoofax3StrategoLanguage implements StrategoLanguage {
    private final ResourceService resourceService;
    private final StrategoParserSelector parserSelector;
    private final StrategoParseWrapper parse;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final ITermFactory termFactory;
    private final StrIncrContext strContext;


    @Inject
    public Spoofax3StrategoLanguage(
        ResourceService resourceService,
        StrategoParserSelector parserSelector,
        StrategoParseWrapper parse,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        StrIncrContext strContext
    ) {
        this.resourceService = resourceService;
        this.parserSelector = parserSelector;
        this.parse = parse;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.termFactory = strategoRuntimeProvider.get().getTermFactory();
        this.strContext = strContext;
    }


    @Override
    public IStrategoTerm parse(ExecContext context, InputStream inputStream, Charset charset, @Nullable String path) throws Exception {
        @Nullable ResourceKey file = null;
        @Nullable ResourcePath rootDirectoryHint = null;
        if(path != null) {
            try {
                file = resourceService.getResourceKey(ResourceKeyString.parse(path));
            } catch(ResourceRuntimeException e) {
                // ignore exception and do not pass a file hint to the following parse method.
            }
            try {
                @Nullable ResourcePath directory = resourceService.getResourcePath(ResourceKeyString.parse(path));
                while(directory != null) {
                    if(resourceService.getReadableResource(directory.appendRelativePath("spoofaxc.cfg")).exists()) {
                        rootDirectoryHint = directory.getNormalized();
                        break;
                    }
                    directory = directory.getParent();
                }
            } catch(ResourceRuntimeException | IOException e) {
                // ignore exception and do not pass a root directory hint to the following parse method.
            }
        }

        final IStrategoTerm ast;
        if(file != null) {
            final Supplier<Result<IStrategoTerm, JsglrParseException>> supplier = JsglrParseTaskInput.builder(parse)
                .withFile(file)
                .rootDirectoryHintNullable(rootDirectoryHint)
                .buildAstSupplier();
            ast = context.require(supplier).unwrap();
        } else {
            final StrategoParser parser = parserSelector.getParserProvider(context, file, rootDirectoryHint).unwrap().get();
            final String text = new String(IOUtil.toByteArray(inputStream), charset);
            ast = parser.parse(new JsglrParseInput(text, "Module", rootDirectoryHint)).ast;
        }

        // Remove ambiguity that occurs in old table from sdf2table when using JSGLR2 parser
        return new DisambiguateAsAnno(strContext).visit(ast);
    }

    @Override public IStrategoTerm parseRtree(InputStream inputStream) throws Exception {
        // TODO: reduce code duplication with Spoofax2StrategoLanguage.
        final IStrategoTerm ast = new TermReader(termFactory).parseFromStream(inputStream);
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Module") && ast.getSubtermCount() == 2)) {
            if(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Specification") && ast.getSubtermCount() == 1) {
                throw new IOException("Custom library detected with Specification/1 term in RTree file. This is currently not supported");
            }
            throw new ExecException("Did not find Module/2 in RTree file. Found: \n" + ast.toString(2));
        }
        return ast;
    }

    @Override public IStrategoTerm parseStr2Lib(InputStream inputStream) throws Exception {
        // TODO: reduce code duplication with Spoofax2StrategoLanguage.
        final IStrategoTerm ast = new TermReader(termFactory).parseFromStream(inputStream);
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Str2Lib")
            && ast.getSubtermCount() == 3)) {
            throw new ExecException(
                "Did not find Str2Lib/3 in Str2Lib file. Found: \n" + ast.toString(2));
        }
        return ast;
    }

    @Override
    public IStrategoTerm insertCasts(String moduleName, GTEnvironment environment, String projectPath) throws ExecException {
        return callStrategy(environment, projectPath, "stratego2-insert-casts");
    }

    @Override public IStrategoTerm desugar(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-compile-top-level-def");
    }

    @Override public IStrategoTerm toJava(IStrategoList buildInput, String projectPath) throws ExecException {
        return callStrategy(buildInput, projectPath, "stratego2-strj-sep-comp");
    }

    @Override public IStrategoAppl toCongruenceAst(IStrategoTerm ast, String projectPath) throws ExecException {
        return TermUtils.toAppl(callStrategy(ast, projectPath, "stratego2-mk-cong-def"));
    }

    @Override
    public Collection<? extends IStrategoAppl> toCongruenceAsts(Collection<IStrategoTerm> asts, String projectPath) throws ExecException {
        // TODO: reduce code duplication with Spoofax2StrategoLanguage.
        final IStrategoList result = TermUtils.toList(callStrategy(termFactory.makeList(asts), projectPath, "stratego2-mk-cong-defs"));
        final ArrayList<IStrategoAppl> congruences = new ArrayList<>(result.size());
        for(IStrategoTerm t : result) {
            congruences.add(TermUtils.toAppl(t));
        }
        return congruences;
    }

    @Override public IStrategoTerm auxSignatures(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-aux-signatures");
    }

    @Override public IStrategoTerm overlapCheck(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-dyn-rule-overlap-check");
    }

    @Override public IStrategoTerm metaExplode(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "MetaExplode");
    }

    private IStrategoTerm callStrategy(IStrategoTerm input, String rootDirectory, String strategyName) throws ExecException {
        final StrategoRuntime runtime = strategoRuntimeProvider.get();
        final HybridInterpreter hybridInterpreter = runtime.getHybridInterpreter();
        //noinspection deprecation
        hybridInterpreter.getContext().setFactory(strContext.getFactory());
        hybridInterpreter.getCompiledContext().setFactory(strContext.getFactory());
        strContext.resetUsedStringsInFactory();
        try {
            return runtime.invoke(strategyName, input);
        } catch(StrategoException e) {
            throw new ExecException(e);
        }
    }
}
