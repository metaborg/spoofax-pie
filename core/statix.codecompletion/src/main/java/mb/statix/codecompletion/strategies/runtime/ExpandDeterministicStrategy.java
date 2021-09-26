package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.constraints.CUser;
import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.NamedStrategy2;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.runtime.TegoEngine;

import static mb.statix.codecompletion.strategies.runtime.SearchStrategies.*;
import static mb.tego.strategies.StrategyExt.*;
import static mb.tego.strategies.runtime.Strategies.*;

public final class ExpandDeterministicStrategy extends NamedStrategy2<SolverContext, ITermVar, SolverState, Seq<SolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandDeterministicStrategy instance = new ExpandDeterministicStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandDeterministicStrategy getInstance() { return (ExpandDeterministicStrategy)instance; }

    private ExpandDeterministicStrategy() { /* Prevent instantiation. Use getInstance(). */ }

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
        // def expandDeterministic(v: ITermVar) =
        //   fixSet(distinct(try(
        //     select(CUser::class, \(constraint: CUser) SolverState -> SolverState?
        //       = where(let vars = project(v) ; ITerm#getVars in
        //           containsAnyVar(vars, constraint)
        //         )
        //     \) |>
        //     single(
        //       expandPredicate(v) |>
        //       assertValid(v) |>
        //       filterPlaceholder(v)
        //     )
        //   ))
        final Strategy<SolverState, Seq<SolverState>> s = fixSet(distinct(try_(
            seq(select(CUser.class, lam((CUser constraint)
                    -> where(let(seq(fun(SolverState::project).apply(v)).$(fun(ITerm::getVars)).$(), vars ->
                    containsAnyVar(vars, constraint)
                ))
            )))
            .$(flatMap(single(
                seq(expandPredicate(ctx, v))
                .$(flatMap(ntl(assertValid(ctx, v))))
                .$(flatMap(ntl(filterPlaceholder(v))))
                .$()
            )))
            .$()
        )));
        return nn(engine.eval(s, input));
    }

    @Override
    public String getName() {
        return "expandDeterministic";
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
