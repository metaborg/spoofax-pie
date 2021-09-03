package mb.statix.pie;

import mb.aterm.common.TermToString;
import mb.constraint.pie.ConstraintAnalyzeFile;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

public abstract class ShowScopeGraphTaskDef implements TaskDef<ShowScopeGraphTaskDef.Args, CommandFeedback> {
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
            return "ShowScopeGraphTaskDef.Args{" +
                "rootDirectory=" + rootDirectory +
                ", file=" + file +
                '}';
        }
    }


    private final ConstraintAnalyzeFile analyzeFile;
    private final GetStrategoRuntimeProvider getStrategoRuntimeProvider;

    public ShowScopeGraphTaskDef(
        ConstraintAnalyzeFile analyzeFile,
        GetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        this.analyzeFile = analyzeFile;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }


    @Override @SuppressWarnings("RedundantThrows")
    public CommandFeedback exec(ExecContext context, Args input) throws Exception {
        final ResourcePath rootDirectory = input.rootDirectory;
        final ResourceKey file = input.file;
        return context.require(analyzeFile, new ConstraintAnalyzeFile.Input(rootDirectory, file)).mapOrElse(
            output -> {
                final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get().addContextObject(output.context);
                final IStrategoTerm inputTerm = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), output.ast, file.asString(), rootDirectory.asString());
                try {
                    final IStrategoTerm outputTerm = strategoRuntime.invoke("stx--show-scopegraph", inputTerm);
                    final IStrategoTerm scopeGraphTerm;
                    if(outputTerm.getSubtermCount() > 1) { // `(filename, result)` term, interested in `result`.
                        scopeGraphTerm = outputTerm.getSubterm(1);
                    } else { // Unexpected term, just use entire output term.
                        scopeGraphTerm = outputTerm;
                    }
                    return CommandFeedback.of(ShowFeedback.showText(TermToString.toString(scopeGraphTerm), "Scope graph for '" + file + "'"));
                } catch(StrategoException e) {
                    return CommandFeedback.ofTryExtractMessagesFrom(e, file);
                }
            },
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
        );
    }
}
