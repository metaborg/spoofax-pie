package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.codecompletion.CCSolverState;
import mb.statix.codecompletion.SelectedConstraintCCSolverState;
import mb.statix.codecompletion.SolverContext;
import mb.statix.codecompletion.SolverState;
import mb.statix.constraints.CResolveQuery;
import mb.statix.constraints.CUser;
import mb.statix.constraints.IResolveQuery;
import mb.tego.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.Strategy1;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class SearchStrategies {
    private SearchStrategies() { /* Cannot be instantiated. */ }

    public static Strategy<CCSolverState, @Nullable CCSolverState> assertValid(SolverContext ctx, ITermVar v) {
        return AssertValidStrategy.getInstance().apply(ctx, v);
    }

    public static Strategy<CCSolverState, @Nullable CCSolverState> containsVar(ITermVar v, IConstraint constraint) {
        return ContainsAnyVarStrategy.getInstance().apply(Collections.singletonList(v), constraint);
    }

    public static Strategy<CCSolverState, @Nullable CCSolverState> containsAnyVar(Collection<ITermVar> vars, IConstraint constraint) {
        return ContainsAnyVarStrategy.getInstance().apply(vars, constraint);
    }

    public static Strategy<CCSolverState, @Nullable CCSolverState> notYetExpanded(CUser constraint) {
        return NotYetExpandedStrategy.getInstance().apply(constraint);
    }

    public static Strategy<CCSolverState, CCSolverState> delayStuckQueries(SolverContext ctx) {
        return DelayStuckQueriesStrategy.getInstance().apply(ctx);
    }

    public static Strategy<CCSolverState, Seq<CCSolverState>> expandAllInjections(SolverContext ctx, ITermVar v, Set<String> visitedInjections) {
        return ExpandAllInjectionsStrategy.getInstance().apply(ctx, v, visitedInjections);
    }

    public static Strategy<CCSolverState, Seq<CCSolverState>> expandAllInjections(SolverContext ctx, ITermVar v) {
        return ExpandAllPredicatesStrategy.getInstance().apply(ctx, v);
    }

    public static Strategy<CCSolverState, Seq<CCSolverState>> expandAllQueries(SolverContext ctx, ITermVar v) {
        return ExpandAllQueriesStrategy.getInstance().apply(ctx, v);
    }

    public static Strategy<CCSolverState, Seq<CCSolverState>> expandDeterministic(SolverContext ctx, ITermVar v) {
        return ExpandDeterministicStrategy.getInstance().apply(ctx, v);
    }

    public static Strategy<CCSolverState, Seq<CCSolverState>> expandInjection(SolverContext ctx, ITermVar v, Set<String> visitedInjections) {
        return ExpandInjectionStrategy.getInstance().apply(ctx, v, visitedInjections);
    }

    public static Strategy<SelectedConstraintCCSolverState<CUser>, Seq<CCSolverState>> expandPredicate(SolverContext ctx, ITermVar v) {
        return ExpandPredicateStrategy.getInstance().apply(ctx, v);
    }

    public static Strategy<SelectedConstraintCCSolverState<IResolveQuery>, Seq<CCSolverState>> expandQuery(SolverContext ctx, ITermVar v) {
        return ExpandQueryStrategy.getInstance().apply(ctx, v);
    }

    public static Strategy<CCSolverState, @Nullable CCSolverState> filterPlaceholder(ITermVar v) {
        return FilterPlaceholderStrategy.getInstance().apply(v);
    }


    public static Strategy<CCSolverState, CCSolverState> infer() {
        return InferStrategy.getInstance();
    }

//    public static <C extends IConstraint> Strategy<SolverState, Seq<SelectedConstraintSolverState<C>>> select(Class<C> constraintClass, LambdaStrategy1<C, SolverState, @Nullable SolverState> predicate) {
//        return SelectStrategy.<C>getInstance().apply(constraintClass, predicate);
//    }

    public static <C extends IConstraint> Strategy<CCSolverState, Seq<SelectedConstraintCCSolverState<C>>> select(Class<C> constraintClass, Strategy1<C, CCSolverState, @Nullable CCSolverState> predicate) {
        return SelectStrategy.<C>getInstance().apply(constraintClass, predicate);
    }

    public static <C extends IConstraint> Strategy<CCSolverState, Seq<SelectedConstraintCCSolverState<C>>> select(Class<C> constraintClass) {
        return select(constraintClass, (eng, c, i) -> i);
    }

}
