package mb.statix.codecompletion;

import com.google.common.collect.ImmutableMap;
import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.statix.constraints.messages.IMessage;
import mb.statix.solver.Delay;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.ICompleteness;
import mb.statix.spec.ApplyResult;
import mb.statix.spec.Spec;
import mb.tego.utils.TextStringBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.functions.Function2;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Objects;

/**
 * A search state with a selected constraint.
 *
 * @param <C> the type of selected constraint
 */
public final class SelectedConstraintCCSolverState<C extends IConstraint> extends CCSolverState implements ISelectedConstraintSolverState<C> {

    /**
     * Creates a new {@link SolverState} from the given specification, solver state, and constraints.
     *
     * @param selection the selection
     * @param state the solver state
     * @return the resulting search state
     */
    public static <C extends IConstraint> SelectedConstraintCCSolverState<C> of(
        C selection,
        CCSolverState state
    ) {
        return new SelectedConstraintCCSolverState<C>(selection,
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
    private SelectedConstraintCCSolverState(
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

    @Override public <C extends IConstraint> SelectedConstraintCCSolverState<C> withSelected(C newSelection) {
        return new SelectedConstraintCCSolverState<>(newSelection,
            this.spec, this.state, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, this.expanded, this.meta
        );
    }

    @Override public CCSolverState withoutSelected() {
        return new CCSolverState(
            this.spec, this.state, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, this.expanded, this.meta
        );
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withExpanded(Set.Immutable<String> newExpanded) {
        return (SelectedConstraintCCSolverState<C>)super.withExpanded(newExpanded);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withExistentials(Iterable<ITermVar> newExistentials) {
        return (SelectedConstraintCCSolverState<C>)super.withExistentials(newExistentials);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withSingleConstraint() {
        return (SelectedConstraintCCSolverState<C>)super.withSingleConstraint();
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withPrecomputedCriticalEdges() {
        return (SelectedConstraintCCSolverState<C>)super.withPrecomputedCriticalEdges();
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withApplyResult(ApplyResult result, @Nullable IConstraint focus) {
        return (SelectedConstraintCCSolverState<C>)super.withApplyResult(result, focus);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withState(IState.Immutable newState) {
        return (SelectedConstraintCCSolverState<C>)super.withState(newState);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withUpdatedConstraints(Iterable<IConstraint> add, Iterable<IConstraint> remove) {
        return (SelectedConstraintCCSolverState<C>)super.withUpdatedConstraints(add, remove);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withDelays(Iterable<? extends java.util.Map.Entry<IConstraint, Delay>> delays) {
        return (SelectedConstraintCCSolverState<C>)super.withDelays(delays);
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withDelay(IConstraint constraint, Delay delay) {
        return (SelectedConstraintCCSolverState<C>)super.withDelays(Collections.singletonList(new AbstractMap.SimpleEntry<>(constraint, delay)));
    }

    @SuppressWarnings("unchecked")
    @Override public SelectedConstraintCCSolverState<C> withMeta(SolutionMeta newMeta) {
        return (SelectedConstraintCCSolverState<C>)super.withMeta(newMeta);
    }

    /**
     * Creates a copy of this {@link SelectedConstraintCCSolverState} with the specified values.
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
    private SelectedConstraintCCSolverState<C> copy(
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
        return new SelectedConstraintCCSolverState<>(newSelection, newSpec, newState, newMessages, newConstraints, newDelays,
            newExistentials, newCompleteness, newExpanded, newMeta);
    }

    @Override protected SelectedConstraintCCSolverState<C> copy(
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
        final SelectedConstraintCCSolverState<C> that = (SelectedConstraintCCSolverState<C>)o;
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
    private boolean equalsSafe(SelectedConstraintCCSolverState<C> that) {
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
