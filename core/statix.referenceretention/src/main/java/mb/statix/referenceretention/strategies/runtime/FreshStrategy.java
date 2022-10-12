package mb.statix.referenceretention.strategies.runtime;

import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.TermVar;
import mb.statix.solver.IState;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.tuples.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.tuple.Tuple2;

/**
 * Generates a fresh constraint variable.
 */
public final class FreshStrategy extends NamedStrategy1<@Nullable String, RRSolverState, Pair<ITermVar, RRSolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final FreshStrategy instance = new FreshStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static FreshStrategy getInstance() { return (FreshStrategy)instance; }

    private FreshStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "fresh";
    }

    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "RedundantSuppression"})
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "name";
            default: return super.getParamName(index);
        }
    }

    @Override
    public Pair<ITermVar, RRSolverState> evalInternal(
        TegoEngine engine,
        @Nullable String name,
        RRSolverState input
    ) {
        return eval(engine, name, input);
    }

    public static Pair<ITermVar, RRSolverState> eval(
        TegoEngine engine,
        @Nullable String name,
        RRSolverState input
    ) {
        final Tuple2<ITermVar, IState.Immutable> newVarAndState;
        if (name != null) {
            newVarAndState = input.getState().freshVar(TermVar.of("", name));
        } else {
            newVarAndState = input.getState().freshWld();
        }
        final ITermVar v = newVarAndState._1();
        final RRSolverState output = input.withState(newVarAndState._2());
        return Pair.of(v, output);
    }
}
