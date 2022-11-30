package mb.statix.referenceretention.tego;

import mb.statix.solver.completeness.IsComplete;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.tego.strategies.NamedStrategy;
import mb.tego.strategies.runtime.TegoEngine;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

import static mb.statix.solver.persistent.Solver.RETURN_ON_FIRST_ERROR;

/**
 * Delays stuck queries.
 */
public final class InferStrategy extends NamedStrategy<RRSolverState, RRSolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final InferStrategy instance = new InferStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static InferStrategy getInstance() { return instance; }

    private InferStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "infer";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            default: return super.getParamName(index);
        }
    }

    @Override
    public RRSolverState evalInternal(TegoEngine engine, RRSolverState input) {
        return eval(engine, input);
    }

    public static RRSolverState eval(TegoEngine engine, RRSolverState input) {
        try {
            final SolverResult result = Solver.solve(
                input.getSpec(),
                input.getState(),
                input.getConstraints(),
                input.getDelays(),
                input.getCompleteness(),
                IsComplete.ALWAYS,
                new NullDebugContext(),
                new NullProgress(),
                new NullCancel(),
                RETURN_ON_FIRST_ERROR
            );

            // NOTE: Call the isSuccessful() strategy on this result to ensure it has no errors.

            return RRSolverState.fromSolverResult(result, input.getExistentials(), input.getPlaceholderDescriptors());
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}
