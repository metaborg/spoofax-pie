package mb.chars;

import mb.aterm.common.TermToString;
import mb.chars.task.CharsParse;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

public class CharsShowAst implements TaskDef<CharsShowAst.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourceKey file;

        public Args(ResourceKey file) {
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return file.equals(args.file);
        }

        @Override public int hashCode() {
            return Objects.hash(file);
        }

        @Override public String toString() {
            return "Args{" +
                "file=" + file +
                '}';
        }
    }


    private final CharsParse parse;

    @Inject public CharsShowAst(CharsParse parse) {
        this.parse = parse;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        final ResourceKey file = args.file;
        return context
            .require(parse.inputBuilder().withFile(file).buildAstSupplier())
            .mapOrElse(
                ast -> CommandFeedback.of(ShowFeedback.showText(TermToString.toString(ast), "Parsed AST of '" + file + "'")),
                e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
            );
    }
}
