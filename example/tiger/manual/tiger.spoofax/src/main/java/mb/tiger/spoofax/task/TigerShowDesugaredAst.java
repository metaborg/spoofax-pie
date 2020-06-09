package mb.tiger.spoofax.task;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.jsglr.common.TermTracer;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;

public class TigerShowDesugaredAst implements TaskDef<TigerShowArgs, CommandOutput> {
    private final TigerParse parse;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public TigerShowDesugaredAst(
        TigerParse parse,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, TigerShowArgs input) throws Exception {
        final ResourceKey key = input.key;
        final @Nullable Region region = input.region;

        final JSGLR1ParseOutput parseResult = context.require(parse, new ResourceStringSupplier(key));
        final IStrategoTerm ast = parseResult.getAst()
            .orElseThrow(() -> new RuntimeException("Cannot show desugared AST, parsed AST for '" + key + "' is null"));

        final IStrategoTerm term;
        if(region != null) {
            term = TermTracer.getSmallestTermEncompassingRegion(ast, region);
        } else {
            term = ast;
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
        final String strategyId = "desugar-all";
        final @Nullable IStrategoTerm result = strategoRuntime.invoke(strategyId, term);
        if(result == null) {
            throw new RuntimeException("Cannot show desugared AST, executing Stratego strategy '" + strategyId + "' failed");
        }

        final String formatted = StrategoUtil.toString(result);
        return new CommandOutput(ListView.of(CommandFeedback.showText(formatted, "Desugared AST for '" + key + "'")));
    }

    @Override public Task<CommandOutput> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
