package mb.statix.codecompletion;

import io.usethesource.capsule.Set;
import mb.nabl2.terms.ITermVar;
import mb.statix.solver.Delay;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.spec.ApplyResult;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.AbstractMap;
import java.util.Collections;

/**
 * A solver state with a selected constraint.
 *
 * @param <C> the type of selected constraint
 */
public interface ISelectedConstraintSolverState<C extends IConstraint> extends ISolverState {

    /**
     * The selected constraint.
     *
     * @return the selected constraint
     */
    C getSelected();

    /**
     * The unselected constraints.
     *
     * @return the set of unselected constraints
     */
    Set<IConstraint> getUnselected();

//    @Override ISelectedConstraintSolverState<C> withExpanded(Set.Immutable<String> newExpanded);

    @Override ISelectedConstraintSolverState<C> withExistentials(Iterable<ITermVar> existentials);

    @Override ISelectedConstraintSolverState<C> withSingleConstraint();

    @Override ISelectedConstraintSolverState<C> withPrecomputedCriticalEdges();

    @Override ISelectedConstraintSolverState<C> withApplyResult(ApplyResult result, @Nullable IConstraint focus);

    @Override ISelectedConstraintSolverState<C> withState(IState.Immutable newState);

    @Override ISelectedConstraintSolverState<C> withUpdatedConstraints(Iterable<IConstraint> add, Iterable<IConstraint> remove);

    @Override ISelectedConstraintSolverState<C> withDelays(Iterable<? extends java.util.Map.Entry<IConstraint, Delay>> delays);

    @Override ISelectedConstraintSolverState<C> withDelay(IConstraint constraint, Delay delay);

//    @Override ISelectedConstraintSolverState<C> withMeta(SolutionMeta newMeta);


}
