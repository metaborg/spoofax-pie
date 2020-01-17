package mb.tiger.spoofax.task;

import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.Provider;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoIOAgent;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

@LanguageScope
public class TigerListLiteralVals implements TaskDef<Provider<@Nullable IStrategoTerm>, @Nullable String> {
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;

    @Inject
    public TigerListLiteralVals(
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        StrategoRuntime prototypeStrategoRuntime,
        LoggerFactory loggerFactory,
        ResourceService resourceService
    ) {
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable String exec(ExecContext context, Provider<@Nullable IStrategoTerm> astProvider) throws Exception {
        final @Nullable IStrategoTerm ast = context.require(astProvider);
        //noinspection ConstantConditions
        if(ast == null) {
            return null;
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime);
        final String strategyId = "list-of-literal-vals";
        final @Nullable IStrategoTerm result = strategoRuntime.invoke(strategyId, ast, new StrategoIOAgent(loggerFactory, resourceService));
        if(result == null) {
            return null;
        }

        return StrategoUtil.toString(result, Integer.MAX_VALUE);
    }
}
