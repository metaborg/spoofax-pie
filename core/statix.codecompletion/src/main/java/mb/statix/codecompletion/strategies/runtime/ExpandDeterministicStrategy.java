package mb.statix.codecompletion.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.strategies.NamedStrategy1;
import mb.statix.strategies.runtime.TegoEngine;

public final class ExpandDeterministicStrategy extends NamedStrategy1<SolverContext, ITermVar, SolverState, Seq<SolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandDeterministicStrategy instance = new ExpandDeterministicStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandDeterministicStrategy getInstance() { return (ExpandDeterministicStrategy)instance; }

    private ExpandDeterministicStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public Seq<SolverState> evalInternal(
        TegoEngine engine,
        SolverContext x,
        ITermVar v,
        SolverState input
    ) {
        return eval(engine, x, v, input);
    }

    public static Seq<SolverState> eval(
        TegoEngine engine,
        SolverContext x,
        ITermVar v,
        SolverState input
    ) {
        throw new UnsupportedOperationException("Not yet implemented");
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
