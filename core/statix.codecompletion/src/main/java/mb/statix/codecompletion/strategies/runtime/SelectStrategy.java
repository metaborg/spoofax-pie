package mb.statix.codecompletion.strategies.runtime;

import mb.statix.SelectedConstraintSolverState;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy1;
import mb.statix.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Selects constraints for which the given predicate does not fail.
 */
public final class SelectStrategy<C extends IConstraint> extends NamedStrategy2<Class<C>, Strategy1<C, SolverState, @Nullable SolverState>, SolverState, Seq<SelectedConstraintSolverState<C>>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final SelectStrategy instance = new SelectStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <C extends IConstraint> SelectStrategy<C> getInstance() { return (SelectStrategy<C>)instance; }

    private SelectStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "select";
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "constraintClass";
            case 1: return "predicate";
            default: return super.getParamName(index);
        }
    }

    @Override
    public Seq<SelectedConstraintSolverState<C>> evalInternal(
        TegoEngine engine,
        Class<C> constraintClass,
        Strategy1<C, SolverState, @Nullable SolverState> predicate,
        SolverState input
    ) {
        return eval(engine, constraintClass, predicate, input);
    }

    public static <C extends IConstraint> Seq<SelectedConstraintSolverState<C>> eval(
        TegoEngine engine,
        Class<C> constraintClass,
        Strategy1<C, SolverState, @Nullable SolverState> predicate,
        SolverState input
    ) {
        return Seq.from(input.getConstraints())
            .filterIsInstance(constraintClass)
            .filter(c -> engine.eval(predicate, c, input) != null)
            .map(c -> SelectedConstraintSolverState.of(c, input));
    }

}
