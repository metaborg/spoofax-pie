package mb.statix.codecompletion;

import com.google.common.collect.ImmutableMap;
import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.statix.constraints.CAstId;
import mb.statix.constraints.CExists;
import mb.statix.constraints.messages.IMessage;
import mb.statix.constraints.messages.MessageKind;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * The state of the solver.
 */
public interface ISolverState {

    /**
     * The semantic specification.
     *
     * @return the {@link Spec}
     */
    Spec getSpec();

    /**
     * The {@link IState}.
     *
     * @return the {@link IState}
     */
    IState.Immutable getState();

    /**
     * The messages.
     *
     * @return a map relating constraints to messages
     */
    Map.Immutable<IConstraint, IMessage> getMessages();

    /**
     * The set of constraints left to solve.
     *
     * @return a set of unsolved constraints
     */
    Set.Immutable<IConstraint> getConstraints();

    /**
     * The constraints that have been delayed due to critical edges.
     *
     * @return a map relating constraints and their delaus
     */
    Map.Immutable<IConstraint, Delay> getDelays();

    /**
     * The variables that have been existentially quantified in the most top-level constraint;
     * or {@code null} when no constraints have existentially quantified any variables (yet).
     *
     * This is used to be able to find the value assigned to the top-most quantified variables.
     *
     * @return a map relating an existentially quantified term variable and its fresh term variable;
     * or {@code null} when there are no existentially quantified variables (yet)
     */
    @Nullable ImmutableMap<ITermVar, ITermVar> getExistentials();

    /**
     * The completeness of the solver state.
     *
     * The completeness tracks critical edges, based on the constraints that are yet to be solved.
     * The solver keeps the completeness up-to-date bby informing it whenever constraints are added or solved.
     *
     * Whenever attempting to solve a query constraint, the solver checks the completeness to see if there
     * are any critical edges along its resolution paths. If there are, the constraint is delayed on that edge.
     * Once the critical edge is resolved, the query constraint is reactivated and another attempt is done to
     * solve it.
     *
     * @return the completeness
     */
    ICompleteness.Immutable getCompleteness();

    /**
     * The set of names of expanded predicate constraints, used to detect when we are trying to expand
     * a constraint that we've expanded before but which was reintroduced.
     *
     * @return a set of names of predicate constraints
     */
    Set.Immutable<String> getExpanded();

    /**
     * The meta data about the solution.
     *
     * @return the meta data
     */
    SolutionMeta getMeta();

    /**
     * Creates a copy of this {@link ISolverState} with the specified
     * set of names of expanded predicate constraints.
     *
     * @param newExpanded the new set of names of expanded predicate constraints
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withExpanded(Set.Immutable<String> newExpanded);

    /**
     * Creates a copy of this {@link ISolverState} that tracks the specified
     * existentially quantified term variables.
     *
     * This wraps the constraints in a {@link CExists} constraint that
     * will cause the existentials to be added once the constraint is solved.
     *
     * Any previously tracked existentials are discarded.
     *
     * @param existentials the existentials to track
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withExistentials(Iterable<ITermVar> existentials);

    /**
     * Creates a copy of this {@link ISolverState} in which the constraints
     * are folded into a single conjunction.
     *
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withSingleConstraint();

    /**
     * Creates a copy of this {@link ISolverState} in which the critical edges are pre-computed.
     *
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withPrecomputedCriticalEdges();

    /**
     * Creates a copy of this {@link ISolverState} in which the specified {@link ApplyResult}
     * (the result of a single step in the solver) is applied to the solver state.
     *
     * This updates the constraints, completeness, and delays. If the focus constraint is specified,
     * it is removed from the constraint set and completeness.
     *
     * @param result the {@link ApplyResult}
     * @param focus the focus constraint; or {@code null}
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withApplyResult(ApplyResult result, @Nullable IConstraint focus);

    /**
     * Creates a copy of this {@link ISolverState} with the specified {@link IState}.
     *
     * @param newState the new {@link IState}
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withState(IState.Immutable newState);

    /**
     * Creates a copy of this {@link ISolverState} with the specified constraints added and removed.
     *
     * The completeness and delayed constraints are kept in sync.
     * Note that this method assumes that no constraints appear in both add and remove,
     * otherwise the result will be incorrect!
     *
     * @param add the constraints to add
     * @param remove the constraints to remove
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withUpdatedConstraints(Iterable<IConstraint> add, Iterable<IConstraint> remove);

    /**
     * Creates a copy of this {@link ISolverState} with the specified delays added.
     *
     * @param delays the delays to add
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withDelays(Iterable<? extends java.util.Map.Entry<IConstraint, Delay>> delays);

    /**
     * Creates a copy of this {@link ISolverState} with the specified delay added.
     *
     * @param constraint the constraint being delayed
     * @param delay the delay to add
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withDelay(IConstraint constraint, Delay delay);

    /**
     * Creates a copy of this {@link ISolverState} with the specified meta data.
     *
     * @param newMeta the new meta data
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withMeta(SolutionMeta newMeta);

    /**
     * Creates a copy of this {@link ISolverState} with the specified selection.
     *
     * @param constraint the constraint that was selected
     * @return the modified copy, a {@link ISelectedConstraintSolverState}
     */
    <C extends IConstraint> ISelectedConstraintSolverState<C> withSelected(C constraint);

    /**
     * Creates a copy of this {@link ISolverState} without a selection.
     *
     * @return the modified copy of the {@link ISolverState}
     */
    ISolverState withoutSelected();

    /**
     * Projects the specified term variable to a term value (which may be a term variable).
     *
     * @param var the variable to project
     * @return the fully instantiated value associated with the variable;
     * or the variable itself when not found
     */
    default ITerm project(ITermVar var) {
        @Nullable final ImmutableMap<ITermVar, ITermVar> existentials = getExistentials();
        @Nullable final ITermVar var2 = (existentials != null ? existentials.get(var) : null);
        final ITermVar var3 = (var2 != null ? var2 : var);

        return getState().unifier().findRecursive(var3);
    }

    /**
     * Whether any of the messages in this state are error messages.
     *
     * @return {@code true} when the state has error messages; otherwise, {@code false}
     */
    default boolean hasErrors() {
        return getMessages().values().stream().anyMatch(m -> m.kind().equals(MessageKind.ERROR));
    }

    /**
     * Whether any of the messages in this state are serious error messages.
     *
     * Serious error messages are error messages that are not {@code termId} errors
     * and those that are not in the collection of allowed messages.
     *
     * @param allowedMessages a collection of constraint-message entries that are not serious (i.e., allowed messages)
     * @return {@code true} when the state has error messages that serious; otherwise, {@code false}
     */
    default boolean hasSeriousErrors(Collection<java.util.Map.Entry<IConstraint, IMessage>> allowedMessages) {
        return getMessages().entrySet().stream()
            .filter(kv -> kv.getValue().kind().equals(MessageKind.ERROR))   // Only errors
            .filter(kv -> !allowedMessages.contains(kv))                    // That are not previously present
            .filter(kv -> !(kv.getKey() instanceof CAstId))
            .anyMatch(kv ->
                kv.getValue().kind().equals(MessageKind.ERROR) &&           // Only errors
                    !(kv.getKey() instanceof CAstId) &&                     // That are not termId() errors
                    !allowedMessages.contains(kv)                           // That where not previously present
            );
    }

    /**
     * Writes a summary of the messages of the solver state to the specified string builder.
     *
     * @param sb the string builder to write to
     * @param linePrefix the line prefix to use
     * @param prettyPrinter a function that, given a term and a unifier-disunifier,
     *                      produces a string representation of the term.
     */
    void write(TextStringBuilder sb, String linePrefix, Function2<ITerm, IUniDisunifier, String> prettyPrinter);
}
