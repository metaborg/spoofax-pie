package mb.str.incr;

import mb.common.util.IOUtil;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseInput;
import mb.pie.api.ExecException;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.str.StrategoParser;
import mb.str.StrategoScope;
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

@StrategoScope
public class Spoofax3StrategoLanguage implements StrategoLanguage {
    private final ResourceService resourceService;
    private final Provider<StrategoParser> parserProvider;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final StrIncrContext strContext;


    @Inject
    public Spoofax3StrategoLanguage(
        ResourceService resourceService,
        Provider<StrategoParser> parserProvider,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        StrIncrContext strContext
    ) {
        this.resourceService = resourceService;
        this.parserProvider = parserProvider;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.strContext = strContext;
    }


    @Override
    public IStrategoTerm parse(InputStream inputStream, Charset charset, @Nullable String path) throws JsglrParseException, IOException, InterruptedException {
        final StrategoParser parser = parserProvider.get();
        final String text = new String(IOUtil.toByteArray(inputStream), charset);

        @Nullable ResourceKey resourceKey;
        try {
            resourceKey = resourceService.getResourceKey(ResourceKeyString.parse(path));
        } catch(ResourceRuntimeException e) {
            // HACK: ignore exception and do not pass a resource key to the following parse method.
            resourceKey = null;
        }

        final IStrategoTerm ast = parser.parse(new JsglrParseInput(text, "Module", resourceKey)).ast;

        // Remove ambiguity that occurs in old table from sdf2table when using JSGLR2 parser
        return new DisambiguateAsAnno(strContext).visit(ast);
    }

    @Override public IStrategoTerm parseRtree(InputStream inputStream) throws Exception {
        final ITermFactory termFactory = strategoRuntimeProvider.get().getTermFactory();
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

    @Override public IStrategoTerm auxSignatures(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-aux-signatures");
    }

    @Override public IStrategoTerm overlapCheck(IStrategoTerm ast, String projectPath) throws ExecException {
        return callStrategy(ast, projectPath, "stratego2-dyn-rule-overlap-check");
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
