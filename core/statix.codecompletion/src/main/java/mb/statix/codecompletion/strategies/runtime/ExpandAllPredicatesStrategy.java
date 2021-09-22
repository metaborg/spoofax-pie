package mb.statix.codecompletion.strategies.runtime;

import io.usethesource.capsule.Set;
import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.constraints.CUser;
import mb.statix.constraints.messages.IMessage;
import mb.statix.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.Strategy1;
import mb.statix.strategies.StrategyExt;
import mb.statix.strategies.runtime.TegoEngine;

import java.util.Collection;
import java.util.Map;

import static mb.statix.codecompletion.strategies.runtime.SearchStrategies.*;
import static mb.statix.strategies.StrategyExt.*;
import static mb.statix.strategies.runtime.Strategies.*;

public final class ExpandAllPredicatesStrategy extends NamedStrategy2<SolverContext, ITermVar, SolverState, Seq<SolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandAllPredicatesStrategy instance = new ExpandAllPredicatesStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandAllPredicatesStrategy getInstance() { return (ExpandAllPredicatesStrategy)instance; }

    private ExpandAllPredicatesStrategy() { /* Prevent instantiation. Use getInstance(). */ }

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
        // import io/usethesource/capsule::Set.Immutable
        //
        // def expandAllPredicates(v: ITermVar): SolverState -> [SolverState] =
        //     SolverState#withExpanded(Set.Immutable#of) ;
        //     repeat(
        //       limit(1, select(CUser::class, \(constraint: IConstraint) SolverState -> SolverState?
        //           = containsVar(v, constraint) ; checkNotYetExpanded(constraint)
        //       \)) |>
        //       expandPredicate(v) |>
        //       assertValid(v)
        //     )

        // We need to repeat this, because there might be more than one constraint that limit(1, select..) might select.
        // For example, (and this happened), the first selected constraint may be subtypeOf(), which when completed
        // doesn't result in any additional syntax. We first need to expand the next constraint, typeOfType()
        // to get actually a useful result.
        // An example where this happens is in this program, on the $Type placeholder:
        //   let function $ID(): $Type = $Exp in 3 end
        //   debugState(v,
        final Strategy1<Set.Immutable<String>, SolverState, SolverState> SolverState$withExpanded
            = StrategyExt.def("SolverState#withExpanded", "x", fun(SolverState::withExpanded));

        // @formatter:off
        final Strategy<SolverState, Seq<SolverState>> s =
            // Empty the set of expanded things
            seq(SolverState$withExpanded.apply(Set.Immutable.of()))
            .$(repeat(
                seq(limit(1, select(CUser.class, lam((constraint) -> seq(containsVar(v, constraint)).$(notYetExpanded(constraint)).$()))))
                // Expand the focussed rule
                .$(flatMap(expandPredicate(ctx, v)))
                // Perform inference and remove states that have errors
                .$(flatMap(ntl(assertValid(ctx, v))))
                .$()
            ))
            .$();
        // @formatter:on
        return nn(engine.eval(s, input));
    }

    @Override
    public String getName() {
        return "expandAllPredicates";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "allowedErrors";
            case 1: return "v";
            default: return super.getParamName(index);
        }
    }

}
