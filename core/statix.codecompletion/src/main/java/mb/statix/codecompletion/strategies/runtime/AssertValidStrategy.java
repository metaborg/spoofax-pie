package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.constraints.messages.IMessage;
import mb.statix.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.Strategy1;
import mb.statix.strategies.runtime.AssertThatStrategy;
import mb.statix.strategies.runtime.FlatMapStrategy;
import mb.statix.strategies.runtime.NotStrategy;
import mb.statix.strategies.runtime.TegoEngine;

import java.util.Collection;
import java.util.Map;

public final class AssertValidStrategy extends NamedStrategy1<SolverContext, ITermVar, SolverState, SolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final AssertValidStrategy instance = new AssertValidStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static AssertValidStrategy getInstance() { return (AssertValidStrategy)instance; }

    private AssertValidStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public Seq<SolverState> evalInternal(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        SolverState input
    ) {
        return eval(engine, ctx, v, input);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static Seq<SolverState> eval(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        SolverState input
    ) {
        // Tego:
        // def assertValid(v: ITermVar): SolverState -> [SolverState] =
        //    infer |> assertThat(\:- s ->
        //        <SolverState.hasSeriousErrors(<SolverContext.allowedErrors> ctx) ; not> s
        //    \) |> delayStuckQueries

        // IR (ANF & flatmap)
        // def assertValid(v: ITermVar): SolverState -> [SolverState] :- input ->
        //     let s1: SolverState -> [SolverState] = infer in
        //     let r1: [SolverState] = <s1> input in
        //
        //     let s2: SolverState -> Bool = \:- s ->
        //         let s1': SolverContext -> Collection<Map.Entry<IConstraint, IMessage>> = SolverContext.allowedErrors in
        //         let r1': Collection<Map.Entry<IConstraint, IMessage>> = <s1'> ctx in
        //         let s2': SolverState -> Bool = SolverState.hasSeriousErrors(r1') in
        //         let r2': Bool = <s2'> s in
        //         let r3': Bool = <not> r2' in
        //         r3'
        //     \ in
        //     let s3: SolverState -> [SolverState] = assertThat(s2) in
        //     let f3: [SolverState] -> [SolverState] = flatMap(s3) in
        //     let r3: [SolverState] = <f3> r1 in
        //
        //     let s4: SolverState -> [SolverState] = delayStuckQueries in
        //     let f4: [SolverState] -> [SolverState] = flatMap(s4) in
        //     let r4: [SolverState] = <f4> r3 in
        //
        //     r4

        // IR (optimize by merging the application and the evaluation, extracting lambda)
        // def assertValid(v: ITermVar): SolverState -> [SolverState] :- input ->
        //     let r1: [SolverState] = <infer> input in
        //
        //     let s3: SolverState -> [SolverState] = assertThat(__lambda1) in
        //     let r3: [SolverState] = <flatMap(s3)> r1 in
        //
        //     let r4: [SolverState] = <flatMap(delayStuckQueries)> r3 in
        //
        //     r4
        //
        // def __lambda1: SolverState -> Bool :- s ->
        //     let r1': Collection<Map.Entry<IConstraint, IMessage>> = <SolverContext.allowedErrors> ctx in
        //     let r2': Bool = <SolverState.hasSeriousErrors(r1')> s in
        //     let r3': Bool = <not> r2' in
        //     r3'

        final InferStrategy infer = InferStrategy.getInstance();
        final AssertThatStrategy<SolverContext, SolverState> assertThat = AssertThatStrategy.getInstance();
        final FlatMapStrategy<SolverContext, SolverState, SolverState> flatMap = FlatMapStrategy.getInstance();
        final DelayStuckQueriesStrategy delayStuckQueries = DelayStuckQueriesStrategy.getInstance();
        final Strategy<SolverContext, SolverState, Boolean> __lambda1 = AssertValidStrategy::eval__lambda1;

        final Seq<SolverState> r1 = engine.eval(infer, ctx, input);

        final Strategy<SolverContext, SolverState, SolverState> s3 = assertThat.apply(__lambda1);
        final Seq<SolverState> r3 = engine.eval(flatMap, ctx, s3, r1);

        final Seq<SolverState> r4 = engine.eval(flatMap, ctx, delayStuckQueries, r3);

        return r4;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private static Seq<Boolean> eval__lambda1(
        TegoEngine engine,
        SolverContext ctx,
        SolverState s
    ) {
        try {
            final Strategy<SolverContext, SolverContext, Collection<Map.Entry<IConstraint, IMessage>>> SolverContext_allowedErrors
                = (engine1, ctx1, input1) -> Seq.of(input1.getAllowedErrors());
            final Strategy1<SolverContext, Collection<Map.Entry<IConstraint, IMessage>>, SolverState, Boolean> SolverState_hasSeriousErrors
                = (engine12, ctx12, allowedErrors, input12) -> Seq.of(input12.hasSeriousErrors(allowedErrors));
            final NotStrategy<SolverContext> not = NotStrategy.getInstance();

            final Seq<Collection<Map.Entry<IConstraint, IMessage>>> r1 = engine.eval(SolverContext_allowedErrors, ctx, ctx);
            final Seq<Boolean> r2 = engine.eval(SolverState_hasSeriousErrors, ctx, r1.single(), s);
            final Seq<Boolean> r3 = engine.eval(not, ctx, r2.single());

            return r3;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getName() {
        return "expandDeterministic";
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
