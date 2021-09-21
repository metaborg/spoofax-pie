package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.ISolverState;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.constraints.messages.IMessage;
import mb.statix.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.statix.strategies.FunctionStrategy;
import mb.statix.strategies.FunctionStrategy1;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.StrategyExt;
import mb.statix.strategies.runtime.NotStrategy;
import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;

import static mb.statix.strategies.StrategyExt.*;

@SuppressWarnings("RedundantSuppression")
public final class AssertValidStrategy extends NamedStrategy2<SolverContext, SolverContext, ITermVar, SolverState, @Nullable SolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final AssertValidStrategy instance = new AssertValidStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static AssertValidStrategy getInstance() { return (AssertValidStrategy)instance; }

    private AssertValidStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public @Nullable SolverState evalInternal(
        TegoEngine engine,
        SolverContext x,
        SolverContext ctx,
        ITermVar v,
        SolverState input
    ) {
        return eval(engine, x, ctx, v, input);
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "RedundantIfStatement"})
    public static @Nullable SolverState eval(
        TegoEngine engine,
        SolverContext x,
        SolverContext ctx,
        ITermVar v,
        SolverState input
    ) {
        // Tego:
        // def assertValid(v: ITermVar): SolverState -> SolverState =
        //    infer ;
        //    not(SolverState#hasSeriousErrors(<SolverContext#allowedErrors> ctx)) ;
        //    delayStuckQueries

        final InferStrategy infer = InferStrategy.getInstance();
        final DelayStuckQueriesStrategy delayStuckQueries = DelayStuckQueriesStrategy.getInstance();
        final NotStrategy<SolverContext, SolverState, SolverState> not = NotStrategy.getInstance();

        final @Nullable SolverState r1 = engine.eval(infer, x, input);
        if (r1 == null) return null;

        @Nullable final Collection<Map.Entry<IConstraint, IMessage>> allowedErrors = engine.eval(fun(SolverContext::getAllowedErrors), ctx, ctx);
        if (allowedErrors == null) return null;

        final Strategy<SolverContext, SolverState, @Nullable SolverState> s2 = StrategyExt.<SolverContext, Collection<Map.Entry<IConstraint, IMessage>>, SolverState>pred(SolverState::hasSeriousErrors).apply(allowedErrors);
        final Strategy<SolverContext, SolverState, @Nullable SolverState> s3 = not.apply(s2);
        final @Nullable SolverState r3 = engine.eval(s3, ctx, r1);
        if (r3 == null) return null;

        final @Nullable SolverState r4 = engine.eval(delayStuckQueries, x, r3);
        if (r4 == null) return null;

        return r4;
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
