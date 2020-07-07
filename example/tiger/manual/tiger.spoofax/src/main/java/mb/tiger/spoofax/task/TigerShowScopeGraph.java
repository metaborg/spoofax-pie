package mb.tiger.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
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
        return context.require(analyze, new TigerAnalyze.Input(key, parse.createAstSupplier(key)))
            .mapCatching(output -> {
                final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(output.context);
                final ITermFactory termFactory = strategoRuntime.getTermFactory();
                final IStrategoTerm inputTerm = termFactory.makeTuple(output.result.ast, termFactory.makeString(resourceService.toString(key)));
                return StrategoUtil.toString(strategoRuntime.invoke("spoofax3-editor-show-analysis-term", inputTerm));
            })
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Scope graph for '" + key + "'")), e -> CommandFeedback.ofTryExtractMessagesFrom(e, key));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
