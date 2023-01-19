package mb.tiger.spoofax.task;

import mb.aterm.common.TermToString;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.statix.referenceretention.pie.InlineMethodCallTaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.tiger.spoofax.TigerScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;


@TigerScope
public class TigerInlineMethodCallCommand implements TaskDef<TigerShowArgs, CommandFeedback> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
//    private final InlineMethodCallTaskDef inlineMethodCallTaskDef;

    @Inject public TigerInlineMethodCallCommand(
        TigerParse parse,
        TigerAnalyze analyze,
        Provider<StrategoRuntime> strategoRuntimeProvider
//        InlineMethodCallTaskDef inlineMethodCallTaskDef
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
//        this.inlineMethodCallTaskDef = inlineMethodCallTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, TigerShowArgs input) throws Exception {
        final ResourceKey key = input.key;
//        return context.require(inlineMethodCallTaskDef, new InlineMethodCallTaskDef.Input(
//                key,
//                input.region
//            ));
        return null;
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
