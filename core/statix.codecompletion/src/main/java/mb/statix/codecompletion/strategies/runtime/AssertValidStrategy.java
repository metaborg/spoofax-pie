package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.codecompletion.CCSolverState;
import mb.statix.codecompletion.SolverContext;
import mb.statix.codecompletion.SolverState;
import mb.tego.strategies.NamedStrategy2;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.StrategyExt;
import mb.tego.strategies.runtime.NotStrategy;
import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("RedundantSuppression")
public final class AssertValidStrategy extends NamedStrategy2<SolverContext, ITermVar, CCSolverState, @Nullable CCSolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final AssertValidStrategy instance = new AssertValidStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static AssertValidStrategy getInstance() { return (AssertValidStrategy)instance; }

    private AssertValidStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public @Nullable CCSolverState evalInternal(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        CCSolverState input
    ) {
        return eval(engine, ctx, v, input);
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "RedundantIfStatement"})
    public static @Nullable CCSolverState eval(
        TegoEngine engine,
        SolverContext ctx,
        ITermVar v,
        CCSolverState input
    ) {
        // Tego:
        // def assertValid(allowedErrors: Collection<Map.Entry<IConstraint, IMessage>>, v: ITermVar): SolverState -> SolverState =
        //    infer ;
        //    not(SolverState#hasSeriousErrors(allowedErrors)) ;
        //    delayStuckQueries

        final InferStrategy infer = InferStrategy.getInstance();
//        final DelayStuckQueriesStrategy delayStuckQueries = DelayStuckQueriesStrategy.getInstance();
        final NotStrategy<CCSolverState, CCSolverState> not = NotStrategy.getInstance();

        final @Nullable CCSolverState r1 = engine.eval(infer, input);
        if (r1 == null) return null;

        final Strategy<CCSolverState, @Nullable CCSolverState> s2 = StrategyExt.pred(CCSolverState::hasSeriousErrors).apply(ctx.getAllowedErrors());
        final Strategy<CCSolverState, @Nullable CCSolverState> s3 = not.apply(s2);
        final @Nullable CCSolverState r3 = engine.eval(s3, r1);
        if (r3 == null) return null;
        return r3;

//        final @Nullable SolverState r4 = engine.eval(delayStuckQueries, ctx, r3);
//        if (r4 == null) return null;

//        return r4;
    }

    @Override
    public String getName() {
        return "assertValid";
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
