package mb.constraint.pie;

import mb.aterm.common.TermToString;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public abstract class ShowAnalyzedAstTaskDef implements TaskDef<ShowAnalyzedAstTaskDef.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourcePath rootDirectory;
        public final ResourceKey file;

        public Args(ResourcePath rootDirectory, ResourceKey file) {
            this.rootDirectory = rootDirectory;
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            if(!rootDirectory.equals(args.rootDirectory)) return false;
            return file.equals(args.file);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + file.hashCode();
            return result;
        }

        @Override public String toString() {
            return "ShowAnalyzedAstTaskDef.Args{" +
                "rootDirectory=" + rootDirectory +
                ", file=" + file +
                '}';
        }
    }


    private final ConstraintAnalyzeFile analyzeFile;

    public ShowAnalyzedAstTaskDef(ConstraintAnalyzeFile analyzeFile) {
        this.analyzeFile = analyzeFile;
    }


    @Override
    public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        final ResourceKey file = args.file;
        return context.require(analyzeFile, new ConstraintAnalyzeFile.Input(args.rootDirectory, file)).mapOrElse(
            output -> CommandFeedback.of(ShowFeedback.showText(TermToString.toString(output.ast), "Analyzed AST for '" + file + "'")),
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
        );
    }
}
