package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.runtime.FixSetStrategy;
import mb.statix.strategies.runtime.FlatMapStrategy;
import mb.statix.strategies.runtime.TegoEngine;
import mb.statix.strategies.runtime.TryStrategy;

import java.util.Set;

public final class ExpandAllInjectionsStrategy extends NamedStrategy2<SolverContext, ITermVar, Set<String>, SolverState, SolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandAllInjectionsStrategy instance = new ExpandAllInjectionsStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandAllInjectionsStrategy getInstance() { return (ExpandAllInjectionsStrategy)instance; }

    private ExpandAllInjectionsStrategy() { /* Prevent instantiation. Use getInstance(). */ }

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
        // def expandAllInjections(v: ITermVar, visitedInjections: Set<String>): SolverState -> SolverState =
        //     fixSet(try(expandInjection(visitedInjections, v))) |> assertValid(v)

        // IR: (ANF)
        // def expandAllInjections(v: ITermVar, visitedInjections: Set<String>): SolverState -> SolverState =
        //     let s1: SolverState -> [SolverState] = expandInjection(visitedInjections, v) in
        //     let s2: SolverState -> [SolverState] = try(s1) in
        //     let s3: SolverState -> [SolverState] = fixSet(s2) in
        //     let s4: SolverState -> [SolverState] = assertValid(v) in
        //     s3 |> s4

        // IR: (explicit input, flatmap (bind))
        // def expandAllInjections(v: ITermVar, visitedInjections: Set<String>): SolverState -> SolverState :- input ->
        //     let s1: SolverState -> [SolverState] = expandInjection(visitedInjections, v) in
        //     let s2: SolverState -> [SolverState] = try(s1) in
        //     let s3: SolverState -> [SolverState] = fixSet(s2) in
        //     let r3: [SolverState] = <s3> input in
        //
        //     let s4: SolverState -> [SolverState] = assertValid(v) in
        //     let f4: [SolverState] -> [SolverState] = flatMap(s4) in
        //     let r4: [SolverState] = <f4> r3
        //
        //     r4

        // IR: (optimize by merging the application and the evaluation)
        // def expandAllInjections(v: ITermVar, visitedInjections: Set<String>): SolverState -> SolverState :- input ->
        //     let s1: SolverState -> [SolverState] = expandInjection(visitedInjections, v) in
        //     let s2: SolverState -> [SolverState] = try(s1) in
        //     let r3: [SolverState] = <fixSet(s2)> input in
        //
        //     let s4: SolverState -> [SolverState] = assertValid(v) in
        //     let r4: [SolverState] = <flatMap(s4)> r3
        //
        //     r4

        // NOTE: Here we optimize getting the strategies to the start of the method.
        final ExpandInjectionStrategy expandInjection = ExpandInjectionStrategy.getInstance();
        final TryStrategy<SolverContext, SolverState> try_ = TryStrategy.getInstance();
        final FixSetStrategy<SolverContext, SolverState> fixSet = FixSetStrategy.getInstance();
        final AssertValidStrategy assertValid = AssertValidStrategy.getInstance();
        final FlatMapStrategy<SolverContext, SolverState, SolverState> flatMap = FlatMapStrategy.getInstance();

        final Strategy<SolverContext, SolverState, SolverState> s1 = expandInjection.apply(v, visitedInjections);
        final Strategy<SolverContext, SolverState, SolverState> s2 = try_.apply(s1);
        final Seq<SolverState> r3 = engine.eval(fixSet, ctx, s2, input);

        final Strategy<SolverContext, SolverState, SolverState> s4 = assertValid.apply(v);
        final Seq<SolverState> r4 = engine.eval(flatMap, ctx, s4, r3);

        return r4;
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
            case 1: return "visitedInjections";
            default: return super.getParamName(index);
        }
    }

}
