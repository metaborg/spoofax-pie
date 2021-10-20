package mb.statix.pie;

import mb.aterm.common.TermToString;
import mb.jsglr.pie.JsglrParseTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

public abstract class ShowPreAnalyzeAstTaskDef implements TaskDef<ShowPreAnalyzeAstTaskDef.Args, CommandFeedback> {
    public static class Args implements Serializable {
        private static final long serialVersionUID = 1L;

        public final ResourceKey file;

        public Args(ResourceKey file) {
            this.file = file;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return file.equals(args.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file);
        }

        @Override
        public String toString() {
            return "ShowPreAnalyzeAstTaskDef.Args{" + "file=" + file + '}';
        }
    }


    private final JsglrParseTaskDef parse;
    private final GetStrategoRuntimeProvider getStrategoRuntimeProvider;

    public ShowPreAnalyzeAstTaskDef(JsglrParseTaskDef parse, GetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        this.parse = parse;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }


    @Override
    public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        final ResourceKey file = args.file;
        return context.require(parse.inputBuilder().withFile(file).buildAstSupplier()).mapOrElse(
            ast -> {
                final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();
                try {
                    final IStrategoTerm outputTerm = strategoRuntime.invoke("pre-analyze", ast);
                    return CommandFeedback.of(ShowFeedback.showText(TermToString.toString(outputTerm), "Pre-analyze AST for '" + file + "'"));
                } catch(StrategoException e) {
                    return CommandFeedback.ofTryExtractMessagesFrom(e, file);
                }
            },
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
        );
    }
}
