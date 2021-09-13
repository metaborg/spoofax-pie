package mb.statix;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.statix.constraints.CConj;
import mb.statix.constraints.CExists;
import mb.statix.constraints.messages.IMessage;
import mb.statix.solver.CriticalEdge;
import mb.statix.solver.Delay;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.Completeness;
import mb.statix.solver.completeness.CompletenessUtil;
import mb.statix.solver.completeness.ICompleteness;
import mb.statix.spec.ApplyResult;
import mb.statix.spec.Spec;
import mb.statix.utils.TextStringBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.collection.CapsuleUtil;
import org.metaborg.util.functions.Function2;
import org.metaborg.util.tuple.Tuple2;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * A search state with a selected constraint.
 *
 * @param <C> the type of selected constraint
 */
public final class SelectedConstraintSolverState<C extends IConstraint> extends SolverState implements ISelectedConstraintSolverState<C> {

    /**
     * Creates a new {@link SolverState} from the given specification, solver state, and constraints.
     *
     * @param selection the selection
     * @param state the solver state
     * @return the resulting search state
     */
    public static <C extends IConstraint> SelectedConstraintSolverState<C> of(
        C selection,
        SolverState state
    ) {
        return new SelectedConstraintSolverState<C>(selection,
            state.spec, state.state, state.messages, state.constraints, state.delays,
            state.existentials, state.completeness, state.expanded, state.meta
        );
    }

    private final C selected;
    private final Set.Immutable<IConstraint> unselected;

    /**
     * Initializes a new instance of the {@link SolverState} class.
     *
     * @param selected the selected constraint
     * @param spec the semantic specification
     * @param state the {@link IState}
     * @param messages the messages
     * @param constraints the unsolved constraints
     * @param delays the delays
     * @param existentials the existentials; or {@code null}
     * @param completeness the completeness
     * @param expanded the names of expanded rules
     * @param meta the meta data
     */
    private SelectedConstraintSolverState(
        C selected,
        Spec spec,
        IState.Immutable state,
        Map.Immutable<IConstraint, IMessage> messages,
        Set.Immutable<IConstraint> constraints,
        Map.Immutable<IConstraint, Delay> delays,
        @Nullable ImmutableMap<ITermVar, ITermVar> existentials,
        ICompleteness.Immutable completeness,
        Set.Immutable<String> expanded,
        SolutionMeta meta
    ) {
        super(spec, state, messages, constraints, delays, existentials, completeness, expanded, meta);

        if(!constraints.contains(selected)) {
            throw new IllegalArgumentException("The focus constraint is not one of the constraints in the state.");
        }
        this.selected = selected;
        this.unselected = constraints.__remove(selected);
    }

    @Override public C getSelected() {
        return selected;
    }

    @Override public Set<IConstraint> getUnselected() {
        return unselected;
    }

    @Override public <C extends IConstraint> SelectedConstraintSolverState<C> withSelected(C newSelection) {
        return new SelectedConstraintSolverState<>(newSelection,
            this.spec, this.state, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, this.expanded, this.meta
        );
    }

    @Override public SolverState withoutSelected() {
        return new SolverState(
            this.spec, this.state, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, this.expanded, this.meta
        );
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintSolverState<C> withExpanded(Set.Immutable<String> newExpanded) {
        return (SelectedConstraintSolverState<C>)super.withExpanded(newExpanded);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintSolverState<C> withExistentials(Iterable<ITermVar> newExistentials) {
        return (SelectedConstraintSolverState<C>)super.withExistentials(newExistentials);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintSolverState<C> withSingleConstraint() {
        return (SelectedConstraintSolverState<C>)super.withSingleConstraint();
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintSolverState<C> withPrecomputedCriticalEdges() {
        return (SelectedConstraintSolverState<C>)super.withPrecomputedCriticalEdges();
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintSolverState<C> withApplyResult(ApplyResult result, @Nullable IConstraint focus) {
        return (SelectedConstraintSolverState<C>)super.withApplyResult(result, focus);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintSolverState<C> withState(IState.Immutable newState) {
        return (SelectedConstraintSolverState<C>)super.withState(newState);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintSolverState<C> withUpdatedConstraints(Iterable<IConstraint> add, Iterable<IConstraint> remove) {
        return (SelectedConstraintSolverState<C>)super.withUpdatedConstraints(add, remove);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintSolverState<C> withDelays(Iterable<? extends java.util.Map.Entry<IConstraint, Delay>> delays) {
        return (SelectedConstraintSolverState<C>)super.withDelays(delays);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintSolverState<C> withMeta(SolutionMeta newMeta) {
        return (SelectedConstraintSolverState<C>)super.withMeta(newMeta);
    }

    /**
     * Creates a copy of this {@link SelectedConstraintSolverState} with the specified values.
     *
     * This method should only invoke the constructor.
     *
     * This method can be overridden in subclasses to invoke the subclass constructor instead.
     *
     * @param newSelection the new selection
     * @param newSpec the new {@link Spec}
     * @param newState the new {@link IState}
     * @param newMessages the new messages
     * @param newConstraints the new constraints
     * @param newDelays the new delays
     * @param newExistentials the new existentials
     * @param newCompleteness the new completness
     * @param newExpanded the new names of expanded rules
     * @param newMeta the new meta data
     * @return the modified copy of the {@link SolverState}
     */
    private SolverState copy(
        C newSelection,
        Spec newSpec,
        IState.Immutable newState,
        Map.Immutable<IConstraint, IMessage> newMessages,
        Set.Immutable<IConstraint> newConstraints,
        Map.Immutable<IConstraint, Delay> newDelays,
        @Nullable ImmutableMap<ITermVar, ITermVar> newExistentials,
        ICompleteness.Immutable newCompleteness,
        Set.Immutable<String> newExpanded,
        SolutionMeta newMeta
    ) {
        return new SelectedConstraintSolverState<>(newSelection, newSpec, newState, newMessages, newConstraints, newDelays,
            newExistentials, newCompleteness, newExpanded, newMeta);
    }

    @Override protected SolverState copy(
        Spec newSpec,
        IState.Immutable newState,
        Map.Immutable<IConstraint, IMessage> newMessages,
        Set.Immutable<IConstraint> newConstraints,
        Map.Immutable<IConstraint, Delay> newDelays,
        @Nullable ImmutableMap<ITermVar, ITermVar> newExistentials,
        ICompleteness.Immutable newCompleteness,
        Set.Immutable<String> newExpanded,
        SolutionMeta newMeta
    ) {
        return copy(this.selected, newSpec, newState, newMessages, newConstraints, newDelays,
            newExistentials, newCompleteness, newExpanded, newMeta);
    }

    @SuppressWarnings("unchecked")
    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null ||  getClass() != o.getClass()) return false;
        final SelectedConstraintSolverState<C> that = (SelectedConstraintSolverState<C>)o;
        // @formatter:off
        return Objects.equals(this.selected, that.selected)
            && this.safeEquals(that);
        // @formatter:on
    }

    /**
     * Compares this object to the specified object for equality.
     *
     * This method assumes that the argument is non-null and of the correct type.
     *
     * @param that the other object to compare
     * @return {@code true} when this object and the specified object are equal;
     * otherwise, {@code false}
     */
    private boolean equalsSafe(SelectedConstraintSolverState<C> that) {
        // @formatter:off
        return Objects.equals(this.selected, that.selected)
            && super.safeEquals(that);
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.selected,
            super.hashCode()
        );
    }

    @Override
    public void write(TextStringBuilder sb, String linePrefix, Function2<ITerm, IUniDisunifier, String> prettyPrinter) {
        final IUniDisunifier unifier = getState().unifier();
        sb.append(linePrefix).appendln("selected:");
        sb.append(linePrefix).appendln("  " + selected.toString(t -> prettyPrinter.apply(t, unifier)));
        super.write(sb, linePrefix, prettyPrinter);
    }

}
