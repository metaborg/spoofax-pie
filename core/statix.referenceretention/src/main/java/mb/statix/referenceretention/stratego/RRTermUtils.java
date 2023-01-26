package mb.statix.referenceretention.stratego;

import mb.constraint.common.ConstraintAnalyzerContext;
import mb.nabl2.terms.ITerm;
import mb.statix.solver.persistent.SolverResult;
import mb.stratego.common.AdaptableContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;

import static mb.nabl2.terms.matching.TermMatch.M;

public final class RRTermUtils {
    private RRTermUtils() { /* Nothing to do. */ }

    public static SolverResult extractFinalSolverResult(ITerm term) throws InterpreterException {
        return M.cases(
                M.appl5("ProjectAnalysis",
                    M.term(),                           // globalScope: Scope
                    M.blobValue(SolverResult.class),    // globalAnalysis: SolverResult
                    M.blobValue(SolverResult.class),    // initialAnalysis: SolverResult
                    M.blobValue(SolverResult.class),    // finalAnalysis: SolverResult
                    M.term(),                           // customAnalysis: CustomAnalysis
                    (t, s, g, i, f, a) -> f             // = finalAnalysis: SolverResult
                ),
                M.appl3("FileAnalysis",
                    M.blobValue(SolverResult.class),    // initialAnalysis: SolverResult
                    M.blobValue(SolverResult.class),    // finalAnalysis: SolverResult
                    M.term(),                           // customAnalysis: CustomAnalysis
                    (t, i, f, a) -> f                   // = finalAnalysis: SolverResult
                ),
                M.blobValue(SolverResult.class)         // SolverResult
            ).match(term)
            .orElseThrow(() -> new InterpreterException("Expected solver result."));
    }

    public static RRStrategoContext unwrapStrategoContext(IContext env) {
        try {
            return AdaptableContext.adaptContextObject(env.contextObject(), RRStrategoContext.class);
        } catch(RuntimeException e) {
            throw new RuntimeException("Could not unwrap RRStrategoContext", e);
        }
    }

    public static ConstraintAnalyzerContext unwrapConstraintAnalyzerContext(IContext env) {
        try {
            return AdaptableContext.adaptContextObject(env.contextObject(), ConstraintAnalyzerContext.class);
        } catch(RuntimeException e) {
            throw new RuntimeException("Could not unwrap RRStrategoContext", e);
        }
    }
}
