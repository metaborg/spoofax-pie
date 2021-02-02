package mb.statix.multilang.utils;

import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.IsComplete;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.LoggerDebugContext;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

public class SolverUtils {

    public static SolverResult partialSolve(Spec spec, IState.Immutable state, IConstraint constraint, IDebugContext debug,
                                            IProgress progress, ICancel cancel) throws InterruptedException {
        final IsComplete isComplete = (s, l, st) -> !state.scopes().contains(s);
        return Solver.solve(spec, state, constraint, isComplete, debug, progress, cancel);
    }

    public static IDebugContext createDebugContext(@Nullable Level logLevel) {
        return logLevel != null ?
            // Statix solver still uses org.metaborg.util.log
            new LoggerDebugContext(LoggerUtils.logger("MLA"), logLevel) : new NullDebugContext();
    }
}
