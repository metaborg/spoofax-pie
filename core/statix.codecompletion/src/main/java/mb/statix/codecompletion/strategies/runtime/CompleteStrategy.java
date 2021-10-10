package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.CodeCompletionStageEventHandler;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy;
import mb.tego.strategies.NamedStrategy2;
import mb.tego.strategies.NamedStrategy3;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.runtime.FlatMapStrategy;
import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

/**
 * The main entry point strategy for code completion.
 */
public final class CompleteStrategy extends NamedStrategy3<SolverContext, ITermVar, Set<String>, SolverState, Seq<SolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final CompleteStrategy instance = new CompleteStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static CompleteStrategy getInstance() { return (CompleteStrategy)instance; }

    private CompleteStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public Seq<SolverState> evalInternal(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        Set<String> visitedInjections,
        SolverState input
    ) {
        return eval(engine, ctx, v, visitedInjections, input);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static Seq<SolverState> eval(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        Set<String> visitedInjections,
        SolverState input
    ) {
        // Tego:
        // def complete(v: ITermVar, visitedInjections: Set<String>): SolverState -> [SolverState] =
        //     expandAllPredicates(v) |>
        //     expandAllInjections(v, visitedInjections) |>
        //     expandAllQueries(v) |>
        //     expandDeterministic(v)

        // IR: (ANF)
        // def complete(v: ITermVar, visitedInjections: Set<String>): SolverState -> [SolverState] =
        //     let s1: SolverState -> [SolverState] = expandAllPredicates(v) in
        //     let s2: SolverState -> [SolverState] = expandAllInjections(v, visitedInjections) in
        //     let s3: SolverState -> [SolverState] = expandAllQueries(v) in
        //     let s4: SolverState -> [SolverState] = expandDeterministic(v) in
        //     s1 |> s2 |> s3 |> s4

        // Note how `flatMap` is basically monad `bind`

        // IR: (explicit input, flatmap (bind))
        // def flatMap<T, R>(T -> [R]): [T] -> [R]
        // def complete(v: ITermVar, visitedInjections: Set<String>): SolverState -> [SolverState] :- input ->
        //     let s1: SolverState -> [SolverState] = expandAllPredicates(v) in
        //     let r1: [SolverState] = <s1> input in
        //
        //     let s2: SolverState -> [SolverState] = expandAllInjections(v, visitedInjections) in
        //     let f2: [SolverState] -> [SolverState] = flatMap(s2) in
        //     let r2: [SolverState] = <f2> r1 in
        //
        //     let s3: SolverState -> [SolverState] = expandAllQueries(v) in
        //     let f3: [SolverState] -> [SolverState] = flatMap(s3) in
        //     let r3: [SolverState] = <f3> r2 in
        //
        //     let s4: SolverState -> [SolverState] = expandDeterministic(v) in
        //     let f4: [SolverState] -> [SolverState] = flatMap(s4) in
        //     let r4: [SolverState] = <f4> r3 in
        //
        //     r4

        // IR: (optimize by merging the application and the evaluation)
        // def complete(v: ITermVar, visitedInjections: Set<String>): SolverState -> [SolverState] :- input ->
        //     let r1: [SolverState] = <expandAllPredicates(v)> input in
        //
        //     let s2: SolverState -> [SolverState] = expandAllInjections(v, visitedInjections) in
        //     let r2: [SolverState] = <flatMap(s2)> r1 in
        //
        //     let s3: SolverState -> [SolverState] = expandAllQueries(v) in
        //     let r3: [SolverState] = <flatMap(s3)> r2 in
        //
        //     let s4: SolverState -> [SolverState] = expandDeterministic(v) in
        //     let r4: [SolverState] = <flatMap(s4)> r3 in
        //
        //     r4

        // NOTE: Here we optimize getting the strategies to the start of the method.
        final ExpandAllPredicatesStrategy expandAllPredicates = ExpandAllPredicatesStrategy.getInstance();
        final ExpandAllInjectionsStrategy expandAllInjections = ExpandAllInjectionsStrategy.getInstance();
        final ExpandAllQueriesStrategy expandAllQueries = ExpandAllQueriesStrategy.getInstance();
        final ExpandDeterministicStrategy expandDeterministic = ExpandDeterministicStrategy.getInstance();
        final FlatMapStrategy<SolverState, SolverState> flatMap = FlatMapStrategy.getInstance();

        // NOTE: Here we optimize by merging the application and the evaluation.
        // It saves a method call and an object creation by combining the application and the evaluation in one call.
        final @Nullable Seq<SolverState> r1 = engine.eval(expandAllPredicates, ctx, v, input);
        if (r1 == null) return Seq.of();

        final Strategy<SolverState, Seq<SolverState>> s2 = expandAllInjections.apply(ctx, v, visitedInjections);
        final @Nullable Seq<SolverState> r2 = engine.eval(flatMap, s2, r1);
        if (r2 == null) return Seq.of();

        final Strategy<SolverState, Seq<SolverState>> s3 = expandAllQueries.apply(ctx, v);
        final @Nullable Seq<SolverState> r3 = engine.eval(flatMap, s3, r2);
        if (r3 == null) return Seq.of();

        final @Nullable Seq<SolverState> r4;
        if (ctx.isCompleteDeterministic()) {
            final Strategy<SolverState, Seq<SolverState>> s4 = expandDeterministic.apply(ctx, v);
            r4 = engine.eval(flatMap, s4, r3);
            if(r4 == null) return Seq.of();
        } else {
            r4 = r3;
        }

        return r4;
    }

    @Override
    public String getName() {
        return "complete";
    }

    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "RedundantSuppression"})
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "v";
            case 1: return "visitedInjections";
            default: return super.getParamName(index);
        }
    }

}
