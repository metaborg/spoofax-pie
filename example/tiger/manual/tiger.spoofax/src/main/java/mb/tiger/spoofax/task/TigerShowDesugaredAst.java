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
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;

public class TigerShowDesugaredAst implements TaskDef<TigerShowArgs, CommandFeedback> {
    private final TigerParse parse;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public TigerShowDesugaredAst(
        TigerParse parse,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, TigerShowArgs input) throws Exception {
        final ResourceKey key = input.key;
        final @Nullable Region region = input.region;
        return context
            .require(parse.createAstSupplier(key))
            .map(ast -> {
                if(region != null) {
                    return TermTracer.getSmallestTermEncompassingRegion(ast, region);
                } else {
                    return ast;
                }
            })
            .flatMapOrElse(ast -> {
                try {
                    final String strategyId = "desugar-all";
                    return Result.ofNullableOrElse(
                        strategoRuntimeProvider.get().invoke(strategyId, ast),
                        () -> new Exception("Cannot show desugared AST, invoking '" + strategyId + "' on '" + ast + "' failed unexpectedly")
                    );
                } catch(StrategoException e) {
                    return Result.ofErr(e);
                }
            }, Result::ofErr) // TODO: any way we don't have to use flatMapOrElse that threads the error to convert the type?
            .map(StrategoUtil::toString)
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Desugared AST for '" + key + "'")), e -> CommandFeedback.of(e, key));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
