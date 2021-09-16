package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.runtime.TegoEngine;

import java.util.Set;

public final class ExpandAllPredicatesStrategy extends NamedStrategy1<SolverContext, ITermVar, SolverState, SolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandAllPredicatesStrategy instance = new ExpandAllPredicatesStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandAllPredicatesStrategy getInstance() { return (ExpandAllPredicatesStrategy)instance; }

    private ExpandAllPredicatesStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<SolverState> evalInternal(
        TegoEngine engine,
        SolverContext solverContext,
        ITermVar v,
        SolverState input
    ) {
        // Tego:
        // def expandAllPredicates(v: ITermVar): SolverState -> [SolverState] =
        //     \i: SolverState -> <SolverState#withExpanded(!Set.Immutable#of)> i.\ |>
        //     repeat(
        //       limit(1, select(CUser::class,
        //         \(constraint: IConstraint) state: SolverState -> <containsVar(v, constraint); checkNotYetExpanded(constraint)> state.\
        //       )) |>
        //       expandPredicate(v) |>
        //       assertValid(v)
        //     )
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getName() {
        return "expandAllPredicates";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "v";
            default: return super.getParamName(index);
        }
    }

}
