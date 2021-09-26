package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy2;
import mb.tego.strategies.NamedStrategy3;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.runtime.FixSetStrategy;
import mb.tego.strategies.runtime.FlatMapStrategy;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.strategies.runtime.TryStrategy;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

import static mb.tego.strategies.runtime.Strategies.ntl;

public final class ExpandAllInjectionsStrategy extends NamedStrategy3<SolverContext, ITermVar, Set<String>, SolverState, Seq<SolverState>> {

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
        final TryStrategy<SolverState> try_ = TryStrategy.getInstance();
        final FixSetStrategy<SolverState> fixSet = FixSetStrategy.getInstance();
        final AssertValidStrategy assertValid = AssertValidStrategy.getInstance();
        final FlatMapStrategy<SolverState, SolverState> flatMap = FlatMapStrategy.getInstance();

        final Strategy<SolverState, Seq<SolverState>> s1 = expandInjection.apply(ctx, v, visitedInjections);
        final Strategy<SolverState, Seq<SolverState>> s2 = try_.apply(s1);
        final @Nullable Seq<SolverState> r3 = engine.eval(fixSet, s2, input);
        if (r3 == null) return Seq.of();

        final Strategy<SolverState, Seq<SolverState>> s4 = ntl(assertValid.apply(ctx, v));
        final @Nullable Seq<SolverState> r4 = engine.eval(flatMap, s4, r3);
        if (r4 == null) return Seq.of();

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
            case 0: return "ctx";
            case 1: return "v";
            case 2: return "visitedInjections";
            default: return super.getParamName(index);
        }
    }

}
