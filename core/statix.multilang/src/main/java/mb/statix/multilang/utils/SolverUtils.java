package mb.statix.multilang.utils;

import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.IsComplete;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;

public class SolverUtils {

    public static SolverResult partialSolve(Spec spec, IState.Immutable state, IConstraint constraint, IDebugContext debug) {
        final IsComplete isComplete = (s, l, st) -> !state.scopes().contains(s);
        try {
            return Solver.solve(spec, state, constraint, isComplete, debug);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
