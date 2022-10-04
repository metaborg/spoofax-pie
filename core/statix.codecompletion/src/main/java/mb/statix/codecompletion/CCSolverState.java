package mb.statix.codecompletion;

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
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.ApplyResult;
import mb.statix.spec.Spec;
import mb.tego.utils.TextStringBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.collection.CapsuleUtil;
import org.metaborg.util.functions.Function2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.tuple.Tuple2;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import static mb.statix.solver.persistent.Solver.INCREMENTAL_CRITICAL_EDGES;


/**
 * Code completion solver state.
 */
public class CCSolverState extends SolverState {

    private final static ILogger log = LoggerUtils.logger(CCSolverState.class);

    /**
     * Creates a new {@link CCSolverState} from the given specification, solver state, and constraints.
     *
     * @param spec the semantic specification
     * @param state the solver state
     * @param constraints the constraints
     * @return the resulting search state
     */
    public static CCSolverState of(
        Spec spec,
        IState.Immutable state,
        Iterable<? extends IConstraint> constraints,
        Set.Immutable<String> expanded,
        SolutionMeta meta
    ) {
        final ICompleteness.Transient completeness = Completeness.Transient.of();
        completeness.addAll(constraints, spec, state.unifier());

        return new CCSolverState(spec, state, Map.Immutable.of(), CapsuleUtil.toSet(constraints), Map.Immutable.of(),
            null, completeness.freeze(), expanded, meta);
    }

    /**
     * Creates a new {@link CCSolverState} from the given solver result.
     *
     * @param result the result of inference by the solver
     * @param existentials the new map relating existentially quantified variables with their fresh variable;
     * or {@code null} to use the existing map from the solver result
     * @param expanded the new set of names of expanded rules
     * @param meta the new meta data
     * @return the resulting search state
     */
    public static CCSolverState fromSolverResult(
        SolverResult result,
        @Nullable ImmutableMap<ITermVar, ITermVar> existentials,
        Set.Immutable<String> expanded,
        SolutionMeta meta
    ) {
        final Set.Transient<IConstraint> constraints = Set.Transient.of();
        final Map.Transient<IConstraint, Delay> delays = Map.Transient.of();
        result.delays().forEach((c, d) -> {
            if(d.criticalEdges().isEmpty()) {
                constraints.__insert(c);
            } else {
                delays.__put(c, d);
            }
        });

        final ImmutableMap<ITermVar, ITermVar> newExistentials =
            existentials == null ? result.existentials() : existentials;
        return new CCSolverState(result.spec(), result.state(), CapsuleUtil.toMap(result.messages()),
            constraints.freeze(), delays.freeze(), newExistentials,
            result.completeness(), expanded, meta);
    }

    protected final Set.Immutable<String> expanded;
    protected final SolutionMeta meta;

    /**
     * Initializes a new instance of the {@link CCSolverState} class.
     *
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
    protected CCSolverState(
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
        super(spec, state, messages, constraints, delays, existentials, completeness);
        this.expanded = expanded;
        this.meta = meta;
    }

    /**
     * The set of names of expanded predicate constraints, used to detect when we are trying to expand
     * a constraint that we've expanded before but which was reintroduced.
     *
     * @return a set of names of predicate constraints
     */
    public Set.Immutable<String> getExpanded() {
        return this.expanded;
    }

    /**
     * The meta data about the solution.
     *
     * @return the meta data
     */
    public SolutionMeta getMeta() {
        return this.meta;
    }

    /**
     * Creates a copy of this {@link ISolverState} with the specified
     * set of names of expanded predicate constraints.
     *
     * @param newExpanded the new set of names of expanded predicate constraints
     * @return the modified copy of the {@link ISolverState}
     */
    public CCSolverState withExpanded(Set.Immutable<String> newExpanded) {
        return copy(this.spec, this.state, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, newExpanded, this.meta);
    }

    /**
     * Creates a copy of this {@link ISolverState} with the specified meta data.
     *
     * @param newMeta the new meta data
     * @return the modified copy of the {@link ISolverState}
     */
    public CCSolverState withMeta(SolutionMeta newMeta) {
        return copy(this.spec, this.state, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, this.expanded, newMeta);
    }

    /**
     * Creates a copy of this {@link ISolverState} with the specified selection.
     *
     * @param selection the constraint that was selected
     * @return the modified copy, a {@link ISelectedConstraintSolverState}
     */
    public <C extends IConstraint> SelectedConstraintCCSolverState<C> withSelected(C selection) {
        return SelectedConstraintCCSolverState.of(selection, this);
    }

    /**
     * Creates a copy of this {@link ISolverState} without a selection.
     *
     * @return the modified copy of the {@link ISolverState}
     */
    public CCSolverState withoutSelected() {
        return this;
    }

    @Override public CCSolverState withExistentials(Iterable<ITermVar> existentials) {
        return (CCSolverState)super.withExistentials(existentials);
    }

    @Override public CCSolverState withSingleConstraint() {
        return (CCSolverState)super.withSingleConstraint();
    }

    @Override public CCSolverState withPrecomputedCriticalEdges() {
        return (CCSolverState)super.withPrecomputedCriticalEdges();
    }

    @Override public CCSolverState withApplyResult(ApplyResult result, @Nullable IConstraint focus) {
        return (CCSolverState)super.withApplyResult(result, focus);
    }

    @Override public CCSolverState withUpdatedConstraints(
        java.util.Set<IConstraint> addConstraints,
        java.util.Set<IConstraint> removeConstraints
    ) {
        return (CCSolverState)super.withUpdatedConstraints(addConstraints, removeConstraints);
    }

    @Override public CCSolverState withState(IState.Immutable newState) {
        return (CCSolverState)super.withState(newState);
    }

    @Override public CCSolverState withUpdatedConstraints(Iterable<IConstraint> add, Iterable<IConstraint> remove) {
        return (CCSolverState)super.withUpdatedConstraints(add, remove);
    }

    @Override public CCSolverState withDelays(Iterable<? extends java.util.Map.Entry<IConstraint, Delay>> delays) {
        return (CCSolverState)super.withDelays(delays);
    }

    @Override public CCSolverState withDelay(IConstraint constraint, Delay delay) {
        return (CCSolverState)super.withDelay(constraint, delay);
    }

    @Override protected CCSolverState copy(
        Spec newSpec,
        IState.Immutable newState,
        Map.Immutable<IConstraint, IMessage> newMessages,
        Set.Immutable<IConstraint> newConstraints,
        Map.Immutable<IConstraint, Delay> newDelays,
        @Nullable ImmutableMap<ITermVar, ITermVar> newExistentials,
        ICompleteness.Immutable newCompleteness
    ) {
        return copy(newSpec, newState, newMessages, newConstraints, newDelays,
            newExistentials, newCompleteness, this.expanded, this.meta);
    }

    /**
     * Creates a copy of this {@link CCSolverState} with the specified values.
     *
     * This method should only invoke the constructor.
     *
     * This method can be overridden in subclasses to invoke the subclass constructor instead.
     *
     * @param newSpec         the new {@link Spec}
     * @param newState        the new {@link IState}
     * @param newMessages     the new messages
     * @param newConstraints  the new constraints
     * @param newDelays       the new delays
     * @param newExistentials the new existentials
     * @param newCompleteness the new completness
     * @param newExpanded     the new names of expanded rules
     * @param newMeta         the new meta data
     * @return the modified copy of the {@link CCSolverState}
     */
    protected CCSolverState copy(
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
        return new CCSolverState(newSpec, newState, newMessages, newConstraints, newDelays,
            newExistentials, newCompleteness, newExpanded, newMeta);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CCSolverState that = (CCSolverState)o;
        return this.safeEquals(that);
    }

    /**
     * Compares this object to the specified object for equality.
     * <p>
     * This method assumes that the argument is non-null and of the correct type.
     *
     * @param that the other object to compare
     * @return {@code true} when this object and the specified object are equal;
     * otherwise, {@code false}
     */
    protected boolean safeEquals(CCSolverState that) {
        // @formatter:off
        return super.safeEquals(that)
            && Objects.equals(this.expanded, that.expanded);
            // NOTE: For the purposes of equality, we ignore the meta field
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            super.hashCode(),
            this.expanded
            // NOTE: For the purposes of equality, we ignore the meta field
        );
    }

    @Override public void write(TextStringBuilder sb, String linePrefix, Function2<ITerm, IUniDisunifier, String> prettyPrinter) {
        super.write(sb, linePrefix, prettyPrinter);

        sb.append(linePrefix).appendln("meta:");
        sb.append(linePrefix).append("  expandedQueries: ").appendln(meta.getExpandedQueries());
        sb.append(linePrefix).append("  expandedRules: ").appendln(meta.getExpandedRules());
    }
}
