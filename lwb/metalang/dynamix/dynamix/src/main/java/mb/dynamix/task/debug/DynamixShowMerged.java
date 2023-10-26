package mb.dynamix.task.debug;

import mb.aterm.common.TermToString;
import mb.dynamix.DynamixScope;
import mb.dynamix.task.DynamixAnalyzeFile;
import mb.dynamix.task.DynamixGetStrategoRuntimeProvider;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

@DynamixScope
public class DynamixShowMerged implements TaskDef<DynamixShowMerged.Args, CommandFeedback> {
    public static final class Args implements Serializable {
        public final ResourcePath rootDirectory;
        public final ResourceKey file;

        public Args(ResourcePath rootDirectory, ResourceKey file) {
            this.rootDirectory = rootDirectory;
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Args input = (Args)o;
            if(!rootDirectory.equals(input.rootDirectory)) return false;
            return file.equals(input.file);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + file.hashCode();
            return result;
        }

        @Override public String toString() {
            return "DynamixShowMerged{" +
                "rootDirectory=" + rootDirectory +
                ", file=" + file +
                '}';
        }
    }

    private final DynamixAnalyzeFile analyzeFile;
    private final DynamixGetStrategoRuntimeProvider getStrategoRuntimeProvider;

    @Inject public DynamixShowMerged(
        DynamixAnalyzeFile analyzeFile,
        DynamixGetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        this.analyzeFile = analyzeFile;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) throws IOException {
        final ResourceKey file = args.file;
        return context.require(analyzeFile, new DynamixAnalyzeFile.Input(args.rootDirectory, file)).mapOrElse(
            output -> {
                try {
                    final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get().addContextObject(output.context);

                    final IStrategoTerm compiled = strategoRuntime.invoke("dx--to-merged", output.ast);
                    // TODO: compilation result cannot be pp'd because it contains constructors not in the SDF spec
                    // final IStrategoTerm formatted = strategoRuntime.invoke("pp-dynamix-string", compiled);
                    // return CommandFeedback.of(ShowFeedback.showText(((IStrategoString)formatted).stringValue(), "Merged representation for '" + file + "'"));
                    return CommandFeedback.of(ShowFeedback.showText(TermToString.toString(compiled), "Merged representation for '" + file + "'"));
                } catch(StrategoException e) {
                    final StringBuilder errorSB = new StringBuilder();

                    Throwable ex = e;
                    while(ex != null) {
                        errorSB.append(ex.getMessage() + "\n");
                        ex = ex.getCause();
                    }

                    return CommandFeedback.of(ShowFeedback.showText(errorSB.toString(), "Evaluation error for '" + file + "'"));
                }
            },
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
        );
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
