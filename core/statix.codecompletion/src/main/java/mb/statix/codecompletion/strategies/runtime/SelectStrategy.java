package mb.statix.codecompletion.strategies.runtime;

import mb.statix.SelectedConstraintSolverState;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.runtime.TegoEngine;

import java.util.function.BiPredicate;


/**
 * Selects constraints that match the given predicate.
 */
public final class SelectStrategy<C extends IConstraint> extends NamedStrategy2<SolverContext, Class<C>, BiPredicate<C, SolverState>, SolverState, SelectedConstraintSolverState<C>> {

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
        SolverContext ctx,
        Class<C> constraintClass,
        // TODO: This should probably be a strategy:
        BiPredicate<C, SolverState> predicate,
        SolverState input
    ) {
        return eval(engine, ctx, constraintClass, predicate, input);
    }

    public static <C extends IConstraint> Seq<SelectedConstraintSolverState<C>> eval(
        TegoEngine engine,
        SolverContext ctx,
        Class<C> constraintClass,
        // TODO: This should probably be a strategy:
        BiPredicate<C, SolverState> predicate,
        SolverState input
    ) {
        return Seq.from(input.getConstraints())
            .filterIsInstance(constraintClass)
            .filter(c -> predicate.test(c, input))
            .map(c -> SelectedConstraintSolverState.of(c, input));
    }

}
