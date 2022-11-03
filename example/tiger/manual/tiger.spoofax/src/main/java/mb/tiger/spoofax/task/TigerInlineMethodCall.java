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
import mb.stratego.common.StrategoRuntime;
import mb.tego.tuples.Pair;
import mb.tiger.spoofax.TigerScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;


@TigerScope
public class TigerInlineMethodCall implements TaskDef<TigerShowArgs, CommandFeedback> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public TigerInlineMethodCall(
        TigerParse parse,
        TigerAnalyze analyze,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, TigerShowArgs input) throws Exception {
        final ResourceKey key = input.key;
        final @Nullable Region region = input.region;
        return context
            .require(analyze, new TigerAnalyze.Input(key, parse.inputBuilder().withFile(key).buildAstSupplier()))
            .map(output -> {
                IStrategoTerm regionAst;
                if(region != null) {
                    regionAst = TermTracer.getSmallestTermEncompassingRegion(output.result.analyzedAst, region);
                } else {
                    regionAst = output.result.analyzedAst;
                }
                // inline-method-call(|regionAst)
                return Pair.of(output.result.analyzedAst, regionAst);
            })
            .mapCatching(ast -> TermToString.toString(strategoRuntimeProvider.get().invoke("inline-method-call", ast.component2(), ast.component1())))
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Inlined method call in '" + key + "'")), e -> CommandFeedback.ofTryExtractMessagesFrom(e, key));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
