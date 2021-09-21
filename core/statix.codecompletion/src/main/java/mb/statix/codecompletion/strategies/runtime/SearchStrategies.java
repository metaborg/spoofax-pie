package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.SelectedConstraintSolverState;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.constraints.CResolveQuery;
import mb.statix.constraints.CUser;
import mb.statix.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.Strategy1;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class SearchStrategies {
    private SearchStrategies() { /* Cannot be instantiated. */ }

    public static Strategy<SolverContext, SolverState, @Nullable SolverState> assertValid(ITermVar v) {
        return AssertValidStrategy.getInstance().apply(v);
    }

    public static Strategy<SolverContext, SolverState, @Nullable SolverState> containsVar(ITermVar v, IConstraint constraint) {
        return ContainsAnyVarStrategy.getInstance().apply(Collections.singletonList(v), constraint);
    }

    public static Strategy<SolverContext, SolverState, @Nullable SolverState> containsAnyVar(Collection<ITermVar> vars, IConstraint constraint) {
        return ContainsAnyVarStrategy.getInstance().apply(vars, constraint);
    }

    public static Strategy<SolverContext, SolverState, @Nullable SolverState> notYetExpanded(CUser constraint) {
        return NotYetExpandedStrategy.getInstance().apply(constraint);
    }

    public static Strategy<SolverContext, SolverState, SolverState> delayStuckQueries() {
        return DelayStuckQueriesStrategy.getInstance();
    }

    public static Strategy<SolverContext, SolverState, Seq<SolverState>> expandAllInjections(ITermVar v, Set<String> visitedInjections) {
        return ExpandAllInjectionsStrategy.getInstance().apply(v, visitedInjections);
    }

    public static Strategy<SolverContext, SolverState, Seq<SolverState>> expandAllInjections(ITermVar v) {
        return ExpandAllPredicatesStrategy.getInstance().apply(v);
    }

    public static Strategy<SolverContext, SolverState, Seq<SolverState>> expandAllQueries(ITermVar v) {
        return ExpandAllQueriesStrategy.getInstance().apply(v);
    }

    public static Strategy<SolverContext, SolverState, Seq<SolverState>> expandDeterministic(ITermVar v) {
        return ExpandDeterministicStrategy.getInstance().apply(v);
    }

    public static Strategy<SolverContext, SolverState, Seq<SolverState>> expandInjection(ITermVar v, Set<String> visitedInjections) {
        return ExpandInjectionStrategy.getInstance().apply(v, visitedInjections);
    }

    public static Strategy<SolverContext, SelectedConstraintSolverState<CUser>, Seq<SolverState>> expandPredicate(ITermVar v) {
        return ExpandPredicateStrategy.getInstance().apply(v);
    }

    public static Strategy<SolverContext, SelectedConstraintSolverState<CResolveQuery>, Seq<SolverState>> expandQuery() {
        return ExpandQueryStrategy.getInstance();
    }

    public static Strategy<SolverContext, SolverState, SolverState> infer() {
        return InferStrategy.getInstance();
    }

//    public static <C extends IConstraint> Strategy<SolverContext, SolverState, Seq<SelectedConstraintSolverState<C>>> select(Class<C> constraintClass, LambdaStrategy1<SolverContext, C, SolverState, @Nullable SolverState> predicate) {
//        return SelectStrategy.<C>getInstance().apply(constraintClass, predicate);
//    }

    public static <C extends IConstraint> Strategy<SolverContext, SolverState, Seq<SelectedConstraintSolverState<C>>> select(Class<C> constraintClass, Strategy1<SolverContext, C, SolverState, @Nullable SolverState> predicate) {
        return SelectStrategy.<C>getInstance().apply(constraintClass, predicate);
    }

    public static <C extends IConstraint> Strategy<SolverContext, SolverState, Seq<SelectedConstraintSolverState<C>>> select(Class<C> constraintClass) {
        return select(constraintClass, (eng, ctx, c, i) -> i);
    }

}
