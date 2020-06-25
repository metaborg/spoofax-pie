package mb.tiger.spoofax.task;

import mb.common.region.Region;
import mb.common.result.Result;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class TigerShowAnalyzedAst implements TaskDef<TigerShowArgs, CommandFeedback> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;

    @Inject public TigerShowAnalyzedAst(TigerParse parse, TigerAnalyze analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, TigerShowArgs input) {
        final ResourceKey key = input.key;
        final @Nullable Region region = input.region;
        return context.require(analyze, new TigerAnalyze.Input(key, parse.createAstSupplier(key)))
            .flatMapOrElse((o) -> Result.ofNullableOrElse(
                o.result.ast,
                () -> new Exception("Cannot show analyzed AST, analyzed AST for '" + key + "' is null")
            ), Result::ofErr)
            .map(ast -> {
                if(region != null) {
                    return TermTracer.getSmallestTermEncompassingRegion(ast, region);
                } else {
                    return ast;
                }
            })
            .map(StrategoUtil::toString)
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Analyzed AST for '" + key + "'")), e -> CommandFeedback.of(e, key));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
