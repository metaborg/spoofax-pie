package mb.tiger.spoofax.task;

import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class TigerShowScopeGraph implements TaskDef<TigerShowArgs, CommandFeedback> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;
    private final ResourceService resourceService;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;


    @Inject public TigerShowScopeGraph(
        TigerParse parse,
        TigerAnalyze analyze,
        ResourceService resourceService,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.resourceService = resourceService;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
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
                () -> new Exception("Cannot show scope graph, analyzed AST for '" + key + "' is null")
            ), Result::ofErr)
            .flatMapOrElse(ast -> {
                try {
                    final String strategyId = "spoofax3-editor-show-analysis-term";
                    final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
                    final ITermFactory termFactory = strategoRuntime.getTermFactory();
                    final IStrategoTerm inputTerm = termFactory.makeTuple(ast, termFactory.makeString(resourceService.toString(key)));
                    return Result.ofNullableOrElse(
                        strategoRuntime.invoke(strategyId, inputTerm),
                        () -> new Exception("Cannot show scope graph, invoking '" + strategyId + "' on '" + ast + "' failed unexpectedly")
                    );
                } catch(StrategoException e) {
                    return Result.ofErr(e);
                }
            }, Result::ofErr) // TODO: any way we don't have to use flatMapOrElse that threads the error to convert the type?
            .map(StrategoUtil::toString)
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Scope graph for '" + key + "'")), e -> CommandFeedback.of(e, key));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
