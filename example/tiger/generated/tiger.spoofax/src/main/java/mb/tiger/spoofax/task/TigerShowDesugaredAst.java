package mb.tiger.spoofax.task;

import mb.common.region.Region;
import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.jsglr.common.TermTracer;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerShowDesugaredAst implements TaskDef<TigerShowArgs, CommandFeedback> {
    private final TigerParse parse;
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;

    @Inject public TigerShowDesugaredAst(
        TigerParse parse,
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        StrategoRuntime prototypeStrategoRuntime
    ) {
        this.parse = parse;
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, TigerShowArgs input) throws Exception {
        final ResourceKey key = input.key;
        final @Nullable Region region = input.region;

        final Result<JSGLR1ParseOutput, MessagesException> parseResult = context.require(parse, new ResourceStringSupplier(key));
        final IStrategoTerm ast = parseResult.ok()
            .map(o -> o.ast)
            .orElseThrow(() -> new RuntimeException("Cannot show desugared AST, parsed AST for '" + key + "' is null")); // TODO: use Result

        final IStrategoTerm term;
        if(region != null) {
            term = TermTracer.getSmallestTermEncompassingRegion(ast, region);
        } else {
            term = ast;
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime);
        final String strategyId = "desugar-all";
        final @Nullable IStrategoTerm result = strategoRuntime.invoke(strategyId, term);
        if(result == null) {
            throw new RuntimeException("Cannot show desugared AST, executing Stratego strategy '" + strategyId + "' failed");
        }

        final String formatted = StrategoUtil.toString(result);
        return new CommandFeedback(ListView.of(CommandFeedback.showText(formatted, "Desugared AST for '" + key + "'")));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
