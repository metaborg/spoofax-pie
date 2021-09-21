package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.runtime.TegoEngine;

import java.util.Set;

public final class ExpandAllQueriesStrategy extends NamedStrategy1<SolverContext, ITermVar, SolverState, Seq<SolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandAllQueriesStrategy instance = new ExpandAllQueriesStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandAllQueriesStrategy getInstance() { return (ExpandAllQueriesStrategy)instance; }

    private ExpandAllQueriesStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<SolverState> evalInternal(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        SolverState input
    ) {
        return eval(engine, ctx, v, input);
    }

    public static Seq<SolverState> eval(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        SolverState input
    ) {
        // Tego:
        // def expandAllQueries(v: ITermVar) =
        //     distinct(or(id, fixSet(
        //       if(
        //         limit(1, select(CResolveQuery::class, \(constraint: IConstraint) SolverState -> SolverState?
        //           = where(let vars = project(v) ; ITerm#getVars in containsAnyVar(vars, constraint))
        //         \)),
        //         expandQueryConstraint |> assertValid(v),
        //         id
        //       )
        //     )))



//        distinct(or(id(), fixSet(
//            if_(
//                limit(1, //debugSelectCResolveQuery(v,
//                    selectConstraints(CResolveQuery.class, (constraint, state) -> {
//                            final io.usethesource.capsule.Set.Immutable<ITermVar> innerVars = state.project(v).getVars();
//                            return containsAnyVar(innerVars, constraint, state);
//                        }
//                        //)
//                    )),
//                seq(debugCResolveQuery(v,
//                        expandQueryConstraint()
//                    )
//                )
//                    .$(assertValid(v))
//                    .$(),
//                id()
//            )
//        )))
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getName() {
        return "expandAllQueries";
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
