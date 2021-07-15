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
import mb.str.task.spoofax.StrategoParseWrapper;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.Optional;

@StrategoScope
public class StrategoShowParsedAst implements TaskDef<StrategoShowArgs, CommandFeedback> {
    private final StrategoParseWrapper parse;

    @Inject public StrategoShowParsedAst(StrategoParseWrapper parse) {
        this.parse = parse;
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
            .map(TermToString::toString)
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Parsed AST for '" + key + "'")), e -> CommandFeedback.ofTryExtractMessagesFrom(e, key));
    }

    @Override public Task<CommandFeedback> createTask(StrategoShowArgs input) {
        return TaskDef.super.createTask(input);
    }
}
