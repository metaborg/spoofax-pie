package mb.tiger.spoofax.task;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class TigerShowScopeGraph implements TaskDef<TigerShowArgs, CommandOutput> {
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

    @Override public CommandOutput exec(ExecContext context, TigerShowArgs input) throws Exception {
        final ResourceKey key = input.key;

        final Supplier<@Nullable IStrategoTerm> astSupplier = parse.createNullableAstSupplier(key);
        final TigerAnalyze.@Nullable Output output = context.require(analyze, new TigerAnalyze.Input(key, astSupplier));
        if(output == null) {
            throw new RuntimeException("Cannot show scope graph, analysis output for '" + key + "' is null");
        }
        if(output.result.ast == null) {
            throw new RuntimeException("Cannot show scope graph, analyzed AST for '" + key + "' is null");
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
        final String strategyId = "spoofax3-editor-show-analysis-term";
        final ITermFactory termFactory = strategoRuntime.getTermFactory();
        final IStrategoTerm inputTerm = termFactory.makeTuple(output.result.ast, termFactory.makeString(resourceService.toString(key)));
        final @Nullable IStrategoTerm result = strategoRuntime.addContextObject(output.context).invoke(strategyId, inputTerm);
        if(result == null) {
            throw new RuntimeException("Cannot show scope graph, executing Stratego strategy '" + strategyId + "' failed");
        }

        final String formatted = StrategoUtil.toString(result);
        return new CommandOutput(ListView.of(CommandFeedback.showText(formatted, "Scope graph for '" + key + "'")));
    }

    @Override public Task<CommandOutput> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
