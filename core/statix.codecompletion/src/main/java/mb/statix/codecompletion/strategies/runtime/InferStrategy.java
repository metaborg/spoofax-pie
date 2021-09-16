package mb.statix.codecompletion.strategies.runtime;

import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.solver.completeness.IsComplete;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.strategies.NamedStrategy;
import mb.statix.strategies.runtime.TegoEngine;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

/**
 * Delays stuck queries.
 */
public final class InferStrategy extends NamedStrategy<SolverContext, SolverState, Seq<SolverState>> {

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
    public Seq<SolverState> evalInternal(TegoEngine engine, SolverContext ctx, SolverState input) {
        return eval(engine, ctx, input);
    }

    public static Seq<SolverState> eval(TegoEngine engine, SolverContext ctx, SolverState input) {
        try {
            final SolverResult result = Solver.solve(
                ctx.getSpec(),
                input.getState(),
                input.getConstraints(),
                input.getDelays(),
                input.getCompleteness(),
                IsComplete.ALWAYS,
                new NullDebugContext(),
                new NullProgress(),
                new NullCancel(),
                0
            );

            // NOTE: Call the isSuccessful() strategy on this result to ensure it has no errors.

            return Seq.of(SolverState.fromSolverResult(result, input.getExistentials(), input.getExpanded(), input.getMeta()));
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}
