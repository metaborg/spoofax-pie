package mb.sdf3.spoofax.task;

import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

public class SingleFileAnalysisResult implements Serializable {
    public final @Nullable IStrategoTerm analyzedAst;
    public final ConstraintAnalyzerContext constraintAnalyzerContext;

    public SingleFileAnalysisResult(@Nullable IStrategoTerm analyzedAst, ConstraintAnalyzerContext constraintAnalyzerContext) {
        this.analyzedAst = analyzedAst;
        this.constraintAnalyzerContext = constraintAnalyzerContext;
    }

    public static Supplier<SingleFileAnalysisResult> createSupplier(
        ResourcePath project,
        ResourceKey file,
        Sdf3Parse parse,
        Function<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> desugar,
        Sdf3AnalyzeMulti analyze
    ) {
        final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(
            project,
            Sdf3Util.createResourceWalker(),
            Sdf3Util.createResourceMatcher(),
            desugar.mapInput((ctx, i) -> parse.createNullableRecoverableAstSupplier(i))
        );
        return analyze.createSupplier(analyzeInput).map(analysisOutput -> {
            if(analysisOutput == null) {
                throw new RuntimeException("Analysis failed (returned null)");
            }
            final ConstraintAnalyzer.@Nullable Result analysisResult = analysisOutput.result.getResult(file);
            if(analysisResult == null || analysisResult.ast == null) {
                throw new RuntimeException("Analysis succeeded, but no analysis result nor analyzed AST was found for '" + file + "'");
            }
            return new SingleFileAnalysisResult(analysisResult.ast, analysisOutput.context);
        });
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SingleFileAnalysisResult that = (SingleFileAnalysisResult)o;
        return Objects.equals(analyzedAst, that.analyzedAst) &&
            constraintAnalyzerContext.equals(that.constraintAnalyzerContext);
    }

    @Override public int hashCode() {
        return Objects.hash(analyzedAst, constraintAnalyzerContext);
    }
}
