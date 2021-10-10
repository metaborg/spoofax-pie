package mb.tiger.spoofax.task;

import mb.aterm.common.TermToString;
import mb.common.region.Region;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.tiger.spoofax.TigerScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@TigerScope
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
        return context.require(analyze, new TigerAnalyze.Input(key, parse.inputBuilder().withFile(key).buildAstSupplier()))
            .map(output -> {
                if(region != null) {
                    return TermTracer.getSmallestTermEncompassingRegion(output.result.analyzedAst, region);
                } else {
                    return output.result.analyzedAst;
                }
            })
            .map(TermToString::toString)
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Analyzed AST for '" + key + "'")), e -> CommandFeedback.ofTryExtractMessagesFrom(e, key));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
