package mb.tiger.spoofax.task;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.constraint.common.ConstraintAnalyzer;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.Provider;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedbacks;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoUtil;
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
        final ConstraintAnalyzer.@Nullable SingleFileResult analysisResult = context.require(analyze, new TigerAnalyze.Input(key, astProvider));
        if(analysisResult == null) {
            throw new RuntimeException("Cannot show analyzed AST, analysis result for '" + key + "' is null");
        }
        if(analysisResult.ast == null) {
            throw new RuntimeException("Cannot show analyzed AST, analyzed AST for '" + key + "' is null");
        }

        final IStrategoTerm term;
        if(region != null) {
            term = TermTracer.getSmallestTermEncompassingRegion(analysisResult.ast, region);
        } else {
            term = analysisResult.ast;
        }

        final String formatted = StrategoUtil.toString(term);
        return new CommandOutput(ListView.of(CommandFeedbacks.showText(formatted, "Analyzed AST for '" + key + "'", null)));
    }

    @Override public Task<CommandOutput> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
