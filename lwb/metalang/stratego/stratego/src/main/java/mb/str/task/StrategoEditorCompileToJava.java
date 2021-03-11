package mb.str.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.str.StrategoScope;
import mb.str.config.StrategoCompileConfig;

import javax.inject.Inject;
import java.util.ArrayList;

@StrategoScope
public class StrategoEditorCompileToJava implements TaskDef<StrategoCompileConfig, CommandFeedback> {
    @Inject public StrategoEditorCompileToJava(StrategoCompileToJava compileToJava) {
        this.compileToJava = compileToJava;
    }

    private final StrategoCompileToJava compileToJava;


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public CommandFeedback exec(ExecContext context, StrategoCompileConfig input) throws Exception {
        final Result<None, ?> result = context.require(compileToJava, new StrategoCompileToJava.Input(input, new ArrayList<>()));
        return result.mapErrOrElse(e -> CommandFeedback.ofTryExtractMessagesFrom(e, input.mainFile), none -> CommandFeedback.of());
    }
}
