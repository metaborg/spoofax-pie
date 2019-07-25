package mb.tiger.spoofax.taskdef;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.stratego.common.StrategoIOAgent;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.util.Optional;

public class TigerListLiteralVals implements TaskDef<ResourceKey, @Nullable String> {
    private final TigerParse parse;
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;

    @Inject
    public TigerListLiteralVals(
        TigerParse parse,
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        StrategoRuntime prototypeStrategoRuntime,
        LoggerFactory loggerFactory,
        ResourceService resourceService
    ) {
        this.parse = parse;
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable String exec(ExecContext context, ResourceKey key) throws Exception {
        final JSGLR1ParseResult parseResult = context.require(parse, key);
        final Optional<IStrategoTerm> ast = parseResult.getAst();
        if(!ast.isPresent()) {
            return null;
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime);
        final String strategyId = "list-of-literal-vals";
        final @Nullable IStrategoTerm result = strategoRuntime.invoke(strategyId, ast.get(), new StrategoIOAgent(loggerFactory, resourceService));
        if(result == null) {
            return null;
        }

        return StrategoUtil.toString(result, Integer.MAX_VALUE);
    }
}
