package mb.str.task.debug;

import mb.aterm.common.TermToString;
import mb.common.region.Region;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.str.StrategoScope;
import mb.str.task.StrategoParse;
import mb.str.task.spoofax.StrategoParseWrapper;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;

@StrategoScope
public class StrategoShowDesugaredAst implements TaskDef<StrategoShowArgs, CommandFeedback> {
    private final StrategoParseWrapper parse;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public StrategoShowDesugaredAst(
        StrategoParseWrapper parse,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, StrategoShowArgs input) throws Exception {
        final ResourceKey key = input.file;
        final @Nullable Region region = input.region;
        return context
            .require(parse.inputBuilder().withFile(key).rootDirectoryHint(Optional.ofNullable(input.rootDirectoryHint)).buildAstSupplier())
            .map(ast -> {
                if(region != null) {
                    return TermTracer.getSmallestTermEncompassingRegion(ast, region);
                } else {
                    return ast;
                }
            })
            .mapCatching(ast -> TermToString.toString(strategoRuntimeProvider.get().invoke("basic-desugar-top", ast)))
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Desugared AST for '" + key + "'")), e -> CommandFeedback.ofTryExtractMessagesFrom(e, key));
    }

    @Override public Task<CommandFeedback> createTask(StrategoShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
