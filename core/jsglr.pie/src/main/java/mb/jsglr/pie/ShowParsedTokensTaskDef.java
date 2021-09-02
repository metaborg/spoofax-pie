package mb.jsglr.pie;

import mb.common.token.Token;
import mb.jsglr.common.JSGLRTokens;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

public abstract class ShowParsedTokensTaskDef implements TaskDef<ShowParsedTokensTaskDef.Args, CommandFeedback> {
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
            return "ShowParsedTokensTaskDef.Args{" + "file=" + file + '}';
        }
    }


    private final JsglrParseTaskDef parse;

    public ShowParsedTokensTaskDef(JsglrParseTaskDef parse) {
        this.parse = parse;
    }


    @Override
    public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        final ResourceKey file = args.file;
        return context.require(parse.inputBuilder().withFile(file).buildTokensSupplier()).mapOrElse(
            tokens -> CommandFeedback.of(ShowFeedback.showText(printTokens(tokens), "Parsed tokens for '" + file + "'")),
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
        );
    }

    private String printTokens(JSGLRTokens tokens) {
        final StringBuilder sb = new StringBuilder();
        for(Token<IStrategoTerm> token : tokens.tokens) {
            sb.append(token);
            sb.append('\n');
        }
        return sb.toString();
    }
}
