package mb.statix.referenceretention.tego;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.referenceretention.statix.RRPlaceholder;
import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.Strategy1;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.tuples.Pair;

import static mb.tego.strategies.StrategyExt.fun;
import static mb.tego.strategies.StrategyExt.lam;
import static mb.tego.strategies.StrategyExt.let;
import static mb.tego.strategies.runtime.Strategies.first;
import static mb.tego.strategies.runtime.Strategies.flatMap;
import static mb.tego.strategies.runtime.Strategies.flatten;
import static mb.tego.strategies.runtime.Strategies.forEach;
import static mb.tego.strategies.runtime.Strategies.ntl;
import static mb.tego.strategies.runtime.Strategies.repeat;
import static mb.tego.strategies.runtime.Strategies.seq;

public final class UnwrapOrFixAllReferencesStrategy extends NamedStrategy1<RRContext, RRSolverState, Seq<RRSolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final UnwrapOrFixAllReferencesStrategy instance = new UnwrapOrFixAllReferencesStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static UnwrapOrFixAllReferencesStrategy getInstance() { return (UnwrapOrFixAllReferencesStrategy)instance; }

    private UnwrapOrFixAllReferencesStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "unwrapOrFixAllReferences";
    }

    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "RedundantSuppression"})
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "ctx";
            default: return super.getParamName(index);
        }
    }

    @Override
    public Seq<RRSolverState> evalInternal(
        TegoEngine engine,
        RRContext ctx,
        RRSolverState input
    ) {
        return eval(engine, ctx, input);
    }

    /**
     * Fixes all placeholder references.
     *
     * @param engine the Tego engine
     * @param ctx the solver context
     * @param input the input solver state
     * @return a lazy sequence of solver states
     */
    public static Seq<RRSolverState> eval(
        TegoEngine engine,
        RRContext ctx,
        RRSolverState input
    ) {

        // def unwrapOrFixAllReferences(RRContext): RRSolverState -> [RRSolverState]
        // unwrapOrFixAllReferences(ctx) =
        //     repeat(
        //         (v, d) <- first(getAllPlaceholders());
        //         unwrapOrFixReference(ctx, v, d);
        //         flatMap(ntl(assertValid))
        //     )

        // TODO: Do a try(assertValid) or something, and report if it failed

        final AssertValidStrategy assertValid = AssertValidStrategy.getInstance();
        final UnwrapOrFixReferenceStrategy unwrapOrFixReference = UnwrapOrFixReferenceStrategy.getInstance();
        final Strategy1<RRContext, RRSolverState, Seq<Pair<ITermVar, RRPlaceholder>>> getAllPlaceholders =
            fun((RRSolverState i, RRContext c) -> Seq.from(i.getPlaceholders().entrySet()).map(Pair::from));

        final Strategy<RRSolverState, Seq<RRSolverState>> strategy = repeat(
            let(first(getAllPlaceholders.apply(ctx)), (Pair<ITermVar, RRPlaceholder> p) ->
                let(fun(i -> p.component1()), (ITermVar v) ->
                    let(fun(i -> p.component2()), (RRPlaceholder d) ->
                        seq(unwrapOrFixReference.apply(ctx, v, d))
                        // Ensure the constraints are turned into unifications, and that the result is valid
                        .$(flatMap(ntl(assertValid.apply(ctx))))
                        .$()
                    )
                )
            )
        );

        return engine.eval(strategy, input);
    }
//    /**
//     * Fixes all placeholder references.
//     *
//     * @param engine the Tego engine
//     * @param ctx the solver context
//     * @param input the input solver state
//     * @return a lazy sequence of solver states
//     */
//    public static Seq<RRSolverState> eval(
//        TegoEngine engine,
//        RRContext ctx,
//        RRSolverState input
//    ) {
//
//        // def unwrapOrFixAllReferences(RRContext): RRSolverState -> [RRSolverState]
//        // unwrapOrFixAllReferences(ctx) =
//        //     repeat(
//        //         ps <- getAllPlaceholders;
//        //         flatten(forEach(ps, /(p) i ->
//        //              (v, d) <- p;
//        //              unwrapOrFixReference(ctx, v, d);
//        //              flatMap(ntl(assertValid))
//        //          /)
//        //     )
//
//        final AssertValidStrategy assertValid = AssertValidStrategy.getInstance();
//        final UnwrapOrFixReferenceStrategy unwrapOrFixReference = UnwrapOrFixReferenceStrategy.getInstance();
//        final Strategy1<RRContext, RRSolverState, Seq<Pair<ITermVar, RRPlaceholder>>> getAllPlaceholders =
//            fun((RRSolverState i, RRContext c) -> Seq.from(input.getPlaceholders().entrySet()).map(Pair::from));
//
//        final Strategy<RRSolverState, Seq<RRSolverState>> strategy = repeat(
//            let(getAllPlaceholders.apply(ctx), (Seq<Pair<ITermVar, RRPlaceholder>> ps) ->
//                flatten(forEach(ps, lam((Pair<ITermVar, RRPlaceholder> p) ->
//                    let(fun(i -> p.component1()), (ITermVar v) ->
//                        let(fun(i -> p.component2()), (RRPlaceholder d) ->
//                            seq(unwrapOrFixReference.apply(ctx, v, d))
//                            .$(flatMap(ntl(assertValid.apply(ctx))))
//                            .$()
//                        )
//                    )
//                )))
//            )
//        );
//
//        return engine.eval(strategy, input);
//    }
}
