package mb.sdf3.spoofax.task.debug;

import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.spoofax.task.Sdf3AnalyzeMulti;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.SingleFileAnalysisResult;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;

public abstract class ShowAnalyzedTaskDef extends ShowTaskDefShared implements TaskDef<ShowAnalyzedTaskDef.Args, CommandOutput> {
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
    private final Function<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> desugar;
    private final Sdf3AnalyzeMulti analyze;
    private final Function<Supplier<SingleFileAnalysisResult>, @Nullable IStrategoTerm> operation;

    public ShowAnalyzedTaskDef(
        Sdf3Parse parse,
        Function<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> desugar,
        Sdf3AnalyzeMulti analyze,
        Function<Supplier<SingleFileAnalysisResult>, @Nullable IStrategoTerm> operation,
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

    @Override public CommandOutput exec(ExecContext context, Args args) throws Exception {
        final Supplier<SingleFileAnalysisResult> analyzeResultSupplier = SingleFileAnalysisResult.createSupplier(
            args.project, args.file, parse, desugar, analyze
        );
        final @Nullable IStrategoTerm ast = context.require(operation, analyzeResultSupplier);
        if(ast == null) throw new RuntimeException("Transform to " + resultName + "failed (returned null)");
        return provideOutput(args.concrete, ast, args.file);
    }
}
