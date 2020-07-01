package mb.sdf3.spoofax.task.debug;

import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.spoofax.task.Sdf3AnalyzeMulti;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.util.Sdf3Util;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;

public abstract class ShowAnalyzedTaskDef extends ProvideOutputShared implements TaskDef<ShowAnalyzedTaskDef.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourcePath project;
        public final ResourceKey file;
        public final boolean concrete;

        public Args(ResourcePath project, ResourceKey file, boolean concrete) {
            this.project = project;
            this.file = file;
            this.concrete = concrete;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Args args = (Args)o;
            return concrete == args.concrete &&
                project.equals(args.project) &&
                file.equals(args.file);
        }

        @Override public int hashCode() {
            return Objects.hash(project, file, concrete);
        }

        @Override public String toString() {
            return "Args{project=" + project + ", file=" + file + ", concrete=" + concrete + '}';
        }
    }

    private final Sdf3Parse parse;
    private final Function<Supplier<? extends Result<IStrategoTerm, ?>>, Result<IStrategoTerm, ?>> desugar;
    private final Sdf3AnalyzeMulti analyze;
    private final Function<Supplier<? extends Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>>, Result<IStrategoTerm, ?>> operation;

    public ShowAnalyzedTaskDef(
        Sdf3Parse parse,
        Function<Supplier<? extends Result<IStrategoTerm, ?>>, Result<IStrategoTerm, ?>> desugar,
        Sdf3AnalyzeMulti analyze,
        Function<Supplier<? extends Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>>, Result<IStrategoTerm, ?>> operation,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        String prettyPrintStrategy,
        String resultName
    ) {
        super(strategoRuntimeProvider, prettyPrintStrategy, resultName);
        this.parse = parse;
        this.desugar = desugar;
        this.analyze = analyze;
        this.operation = operation;
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) {
        return context
            .require(operation, analyze.createSingleFileOutputSupplier(new Sdf3AnalyzeMulti.Input(args.project, Sdf3Util.createResourceWalker(), Sdf3Util.createResourceMatcher(), desugar.mapInput((ctx, i) -> parse.createRecoverableAstSupplier(i))), args.file))
            .mapOrElse(ast -> provideOutput(args.concrete, ast, args.file), e -> CommandFeedback.ofTryExtractMessagesFrom(e, args.file));
    }
}
