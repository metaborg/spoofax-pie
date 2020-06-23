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
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerShowParsedAst implements TaskDef<TigerShowArgs, CommandFeedback> {
    private final TigerParse parse;

    @Inject public TigerShowParsedAst(TigerParse parse) {
        this.parse = parse;
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
            .orElseThrow(() -> new RuntimeException("Cannot show parsed AST, parsed AST for '" + key + "' is null")); // TODO: use Result

        final IStrategoTerm term;
        if(region != null) {
            term = TermTracer.getSmallestTermEncompassingRegion(ast, region);
        } else {
            term = ast;
        }

        final String formatted = StrategoUtil.toString(term);
        return new CommandFeedback(ListView.of(CommandFeedback.showText(formatted, "Parsed AST for '" + key + "'")));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
