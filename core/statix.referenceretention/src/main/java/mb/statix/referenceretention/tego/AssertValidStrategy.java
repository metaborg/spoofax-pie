package mb.statix.referenceretention.tego;

import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.StrategyExt;
import mb.tego.strategies.runtime.NotStrategy;
import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("RedundantSuppression")
public final class AssertValidStrategy extends NamedStrategy1<RRContext, RRSolverState, @Nullable RRSolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final AssertValidStrategy instance = new AssertValidStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static AssertValidStrategy getInstance() { return (AssertValidStrategy)instance; }

    private AssertValidStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public @Nullable RRSolverState evalInternal(
        TegoEngine engine,
        RRContext ctx,
        RRSolverState input
    ) {
        return eval(engine, ctx, input);
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "RedundantIfStatement"})
    public static @Nullable RRSolverState eval(
        TegoEngine engine,
        RRContext ctx,
        RRSolverState input
    ) {
        // Tego:
        // def assertValid(allowedErrors: Collection<Map.Entry<IConstraint, IMessage>>, v: ITermVar): SolverState -> SolverState =
        //    infer ;
        //    not(SolverState#hasSeriousErrors(allowedErrors))

        final InferStrategy infer = InferStrategy.getInstance();
        final NotStrategy<RRSolverState, RRSolverState> not = NotStrategy.getInstance();

        final @Nullable RRSolverState r1 = engine.eval(infer, input);
        if (r1 == null) {
            engine.log(instance, "Infer strategy failed on: {}", input);
            return null;
        }

        final Strategy<RRSolverState, @Nullable RRSolverState> s2 = StrategyExt.pred(RRSolverState::hasSeriousErrors).apply(ctx.getAllowedErrors());
        final Strategy<RRSolverState, @Nullable RRSolverState> s3 = not.apply(s2);
        final @Nullable RRSolverState r3 = engine.eval(s3, r1);
        if (r3 == null) {
            engine.log(instance, "Errors occurred, rejecting input: {}", input);
            return null;
        }
        return r3;
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
