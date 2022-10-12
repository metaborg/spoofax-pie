package mb.statix.referenceretention.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.tuples.Pair;

/**
 * Adds a new reference retention placeholder with the specified placeholder descriptor
 * to the solver state under a fresh constraint variable, and returns both the
 * new solver state and the generated fresh constraint variable.
 */
public final class NewPlaceholderStrategy extends NamedStrategy1<RRPlaceholderDescriptor, RRSolverState, Pair<ITermVar, RRSolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final NewPlaceholderStrategy instance = new NewPlaceholderStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static NewPlaceholderStrategy getInstance() { return (NewPlaceholderStrategy)instance; }

    private NewPlaceholderStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "newPlaceholder";
    }

    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "RedundantSuppression"})
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "descriptor";
            default: return super.getParamName(index);
        }
    }

    @Override
    public Pair<ITermVar, RRSolverState> evalInternal(
        TegoEngine engine,
        RRPlaceholderDescriptor descriptor,
        RRSolverState input
    ) {
        return eval(engine, descriptor, input);
    }

    public static Pair<ITermVar, RRSolverState> eval(
        TegoEngine engine,
        RRPlaceholderDescriptor descriptor,
        RRSolverState input
    ) {
        final FreshStrategy fresh = FreshStrategy.getInstance();
        final Pair<ITermVar, RRSolverState> newVarAndState = engine.eval(fresh, (String)null, input);
        final ITermVar v = newVarAndState.component1();
        final RRSolverState newSolverState = newVarAndState.component2();
        final RRSolverState output = newSolverState.addPlaceholder(v, descriptor);
        return Pair.of(v, output);
    }
}
