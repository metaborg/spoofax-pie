package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.runtime.FlatMapStrategy;
import mb.statix.strategies.runtime.TegoEngine;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.Set;

/**
 * The main entry point strategy for code completion.
 */
public final class CompleteStrategy extends NamedStrategy2<SolverContext, ITermVar, Set<String>, SolverState, SolverState> {

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
        final FlatMapStrategy<SolverContext, SolverState, SolverState> flatMap = FlatMapStrategy.getInstance();

        // NOTE: Here we optimize by merging the application and the evaluation.
        // It saves a method call and an object creation by combining the application and the evaluation in one call.
        final Seq<SolverState> r1 = engine.eval(expandAllPredicates, ctx, v, input);

        final Strategy<SolverContext, SolverState, SolverState> s2 = expandAllInjections.apply(v, visitedInjections);
        final Seq<SolverState> r2 = engine.eval(flatMap, ctx, s2, r1);

        final Strategy<SolverContext, SolverState, SolverState> s3 = expandAllQueries.apply(v);
        final Seq<SolverState> r3 = engine.eval(flatMap, ctx, s3, r2);

        final Strategy<SolverContext, SolverState, SolverState> s4 = expandDeterministic.apply(v);
        final Seq<SolverState> r4 = engine.eval(flatMap, ctx, s4, r3);

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
