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
import mb.tiger.spoofax.TigerScope;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;

@TigerScope
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
            .require(parse.inputBuilder().withFile(key).buildAstSupplier())
            .map(ast -> {
                if(region != null) {
                    return TermTracer.getSmallestTermEncompassingRegion(ast, region);
                } else {
                    return ast;
                }
            })
            .mapCatching(ast -> TermToString.toString(strategoRuntimeProvider.get().invoke("desugar-all", ast)))
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Desugared AST for '" + key + "'")), e -> CommandFeedback.ofTryExtractMessagesFrom(e, key));
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
