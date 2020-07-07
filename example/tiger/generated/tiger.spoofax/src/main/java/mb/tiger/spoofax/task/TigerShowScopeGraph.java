package mb.tiger.spoofax.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;

public class TigerShowScopeGraph implements TaskDef<TigerShowArgs, CommandFeedback> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;
    private final ResourceService resourceService;
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;


    @Inject public TigerShowScopeGraph(
        TigerParse parse,
        TigerAnalyze analyze,
        ResourceService resourceService,
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        StrategoRuntime prototypeStrategoRuntime
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.resourceService = resourceService;
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, TigerShowArgs input) throws Exception {
        final ResourceKey key = input.key;

        final Supplier<@Nullable IStrategoTerm> astSupplier = parse.createAstSupplier(key).map(Result::get); // TODO: use Result
        final TigerAnalyze.@Nullable Output output = context.require(analyze, new TigerAnalyze.Input(key, astSupplier));
        if(output == null) {
            throw new RuntimeException("Cannot show scope graph, analysis output for '" + key + "' is null");
        }
        if(output.result.ast == null) {
            throw new RuntimeException("Cannot show scope graph, analyzed AST for '" + key + "' is null");
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime);
        final String strategyId = "spoofax3-editor-show-analysis-term";
        final ITermFactory termFactory = strategoRuntime.getTermFactory();
        final IStrategoTerm inputTerm = termFactory.makeTuple(output.result.ast, termFactory.makeString(resourceService.toString(key)));
        final @Nullable IStrategoTerm result = strategoRuntime.addContextObject(output.context).invoke(strategyId, inputTerm);
        if(result == null) {
            throw new RuntimeException("Cannot show scope graph, executing Stratego strategy '" + strategyId + "' failed");
        }

        final String formatted = StrategoUtil.toString(result);
        return new CommandFeedback(ListView.of(CommandFeedback.showText(formatted, "Scope graph for '" + key + "'")));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
