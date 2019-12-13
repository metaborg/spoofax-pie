package mb.tiger.spoofax.taskdef.command;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.jsglr.common.TermTracer;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedbacks;
import mb.spoofax.core.language.command.CommandInput;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.taskdef.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerShowParsedAstTest implements TaskDef<CommandInput<TigerShowArgs>, CommandOutput> {
    private final TigerParse parse;

    @Inject public TigerShowParsedAstTest(TigerParse parse) {
        this.parse = parse;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, CommandInput<TigerShowArgs> input) throws Exception {
        final ResourceKey key = input.args.key;
        final @Nullable Region region = input.args.region;

        final JSGLR1ParseResult parseResult = context.require(parse, key);
        final IStrategoTerm ast = parseResult.getAst()
            .orElseThrow(() -> new RuntimeException("Cannot show parsed AST, parsed AST for '" + key + "' is null"));

        final IStrategoTerm term;
        if(region != null) {
            term = TermTracer.getSmallestTermEncompassingRegion(ast, region);
        } else {
            term = ast;
        }

        final String formatted = StrategoUtil.toString(term);
        return new CommandOutput(ListView.of(CommandFeedbacks.showText(formatted, "Parsed AST for '" + key + "'", null)));
    }

    @Override public Task<CommandOutput> createTask(CommandInput<TigerShowArgs> input) {
        return TaskDef.super.createTask(input);
    }
}
