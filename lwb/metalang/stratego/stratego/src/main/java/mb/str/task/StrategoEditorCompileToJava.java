package mb.str.task;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.str.StrategoScope;
import mb.str.config.StrategoCompileConfig;

import javax.inject.Inject;

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
        return context.require(compileToJava, input)
            .mapErrOrElse(e -> CommandFeedback.ofTryExtractMessagesFrom(e, input.mainModule.path), () -> CommandFeedback.of());
    }
}
