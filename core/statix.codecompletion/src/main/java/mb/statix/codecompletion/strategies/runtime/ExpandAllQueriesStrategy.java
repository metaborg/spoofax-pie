package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.SelectedConstraintSolverState;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.constraints.CResolveQuery;
import mb.statix.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.StrategyExt;
import mb.statix.strategies.runtime.Strategies;
import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

import static mb.statix.codecompletion.strategies.runtime.SearchStrategies.*;
import static mb.statix.strategies.StrategyExt.*;
import static mb.statix.strategies.runtime.Strategies.*;

public final class ExpandAllQueriesStrategy extends NamedStrategy2<SolverContext, ITermVar, SolverState, Seq<SolverState>> {

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
        //           = where(let vars = project(v) ; ITerm#getVars in
        //               containsAnyVar(vars, constraint)
        //             )
        //         \)),
        //         expandQueryConstraint |> assertValid(v),
        //         id
        //       )
        //     )))
        final Strategy<SolverState, Seq<SolverState>> s = distinct(or(ntl(id()), fixSet(
            if_(
                limit(1, SearchStrategies.select(CResolveQuery.class, lam((CResolveQuery constraint)
                    -> where(let(seq(fun(SolverState::project).apply(v)).$(fun(ITerm::getVars)).$(), vars ->
                        containsAnyVar(vars, constraint)
                    ))
                ))),
                flatMap(seq(expandQuery(ctx)).$(flatMap(ntl(assertValid(ctx, v)))).$()),
                ntl(id())
            )
        )));
        return nn(engine.eval(s, input));
    }

    @Override
    public String getName() {
        return "expandAllQueries";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "ctx";
            case 1: return "v";
            default: return super.getParamName(index);
        }
    }

}
