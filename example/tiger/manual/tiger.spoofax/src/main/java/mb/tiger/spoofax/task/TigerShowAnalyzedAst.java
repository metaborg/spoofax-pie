package mb.tiger.spoofax.task;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.Provider;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerShowAnalyzedAst implements TaskDef<TigerShowArgs, CommandOutput> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;

    @Inject public TigerShowAnalyzedAst(TigerParse parse, TigerAnalyze analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, TigerShowArgs input) throws Exception {
        final ResourceKey key = input.key;
        final @Nullable Region region = input.region;

        final Provider<@Nullable IStrategoTerm> astProvider = parse.createAstProvider(key);
        final TigerAnalyze.@Nullable Output output = context.require(analyze, new TigerAnalyze.Input(key, astProvider));
        if(output == null) {
            throw new RuntimeException("Cannot show analyzed AST, analysis output for '" + key + "' is null");
        }
        if(output.result.ast == null) {
            throw new RuntimeException("Cannot show analyzed AST, analyzed AST for '" + key + "' is null");
        }

        final IStrategoTerm term;
        if(region != null) {
            term = TermTracer.getSmallestTermEncompassingRegion(output.result.ast, region);
        } else {
            term = output.result.ast;
        }

        final String formatted = StrategoUtil.toString(term);
        return new CommandOutput(ListView.of(CommandFeedback.showText(formatted, "Analyzed AST for '" + key + "'")));
    }

    @Override public Task<CommandOutput> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
