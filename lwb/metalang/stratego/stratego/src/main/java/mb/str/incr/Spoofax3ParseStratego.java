package mb.str.incr;

import mb.common.util.IOUtil;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.pie.api.ExecException;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.str.StrategoParser;
import mb.str.StrategoScope;
import mb.stratego.build.strincr.ParseStratego;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@StrategoScope
public class Spoofax3ParseStratego implements ParseStratego {
    private final ResourceService resourceService;
    private final Provider<StrategoParser> parserProvider;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;


    @Inject
    public Spoofax3ParseStratego(ResourceService resourceService, Provider<StrategoParser> parserProvider, Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.resourceService = resourceService;
        this.parserProvider = parserProvider;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }


    @Override
    public IStrategoTerm parse(InputStream inputStream, Charset charset, @Nullable String path) throws JSGLR1ParseException, IOException, InterruptedException {
        final StrategoParser parser = parserProvider.get();
        final String text = new String(IOUtil.toByteArray(inputStream), charset);

        @Nullable ResourceKey resourceKey;
        try {
            resourceKey = resourceService.getResourceKey(ResourceKeyString.parse(path));
        } catch(ResourceRuntimeException e) {
            // HACK: ignore exception and do not pass a resource key to the following parse method.
            resourceKey = null;
        }

        if(resourceKey != null) {
            return parser.parse(text, "Module", resourceKey).ast;
        } else {
            return parser.parse(text, "Module").ast;
        }
    }

    @Override public IStrategoTerm parseRtree(InputStream inputStream) throws Exception {
        final ITermFactory termFactory = strategoRuntimeProvider.get().getTermFactory();
        // TODO: reduce code duplication with Spoofax2ParseStratego.
        final IStrategoTerm ast = new TermReader(termFactory).parseFromStream(inputStream);
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Module") && ast.getSubtermCount() == 2)) {
            if(!(TermUtils.isAppl(ast) && ((IStrategoAppl)ast).getName().equals("Specification") && ast.getSubtermCount() == 1)) {
                throw new ExecException("Did not find Module/2 in RTree file. Found: \n" + ast.toString(2));
            } else {
                throw new ExecException("Bug in custom library detection. Please file a bug report");
            }
        }
        return ast;
    }
}
