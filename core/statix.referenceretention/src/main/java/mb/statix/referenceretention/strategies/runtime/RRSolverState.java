package mb.statix.referenceretention.strategies.runtime;

import com.google.common.collect.ImmutableMap;
import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.statix.codecompletion.ISolverState;
import mb.statix.codecompletion.SolverState;
import mb.statix.constraints.messages.IMessage;
import mb.statix.solver.Delay;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.completeness.Completeness;
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

import java.util.Objects;

/**
 * Code completion solver state.
 */
public class RRSolverState extends SolverState {

    private final static ILogger log = LoggerUtils.logger(RRSolverState.class);

    /**
     * Creates a new {@link RRSolverState} from the given specification, solver state, and constraints.
     *
     * @param spec the semantic specification
     * @param state the solver state
     * @param constraints the constraints
     * @param placeholderDescriptors a map from meta variables to placeholder descriptors
     * @return the resulting search state
     */
    public static RRSolverState of(
        Spec spec,
        IState.Immutable state,
        Iterable<? extends IConstraint> constraints,
        Map.Immutable<ITermVar, RRPlaceholderDescriptor> placeholderDescriptors
    ) {
        final ICompleteness.Transient completeness = Completeness.Transient.of();
        completeness.addAll(constraints, spec, state.unifier());

        return new RRSolverState(spec, state, Map.Immutable.of(), CapsuleUtil.toSet(constraints), Map.Immutable.of(),
            null, completeness.freeze(), placeholderDescriptors);
    }

    /**
     * Creates a new {@link RRSolverState} from the given solver result.
     *
     * @param result the result of inference by the solver
     * @param existentials the new map relating existentially quantified variables with their fresh variable;
     * or {@code null} to use the existing map from the solver result
     * @param placeholderDescriptors a map from meta variables to placeholder descriptors
     * @return the resulting search state
     */
    public static RRSolverState fromSolverResult(
        SolverResult result,
        @Nullable ImmutableMap<ITermVar, ITermVar> existentials,
        Map.Immutable<ITermVar, RRPlaceholderDescriptor> placeholderDescriptors
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
        return new RRSolverState(result.spec(), result.state(), CapsuleUtil.toMap(result.messages()),
            constraints.freeze(), delays.freeze(), newExistentials,
            result.completeness(), placeholderDescriptors);
    }

    protected final Map.Immutable<ITermVar, RRPlaceholderDescriptor> placeholderDescriptors;

    /**
     * Initializes a new instance of the {@link RRSolverState} class.
     *
     * @param spec the semantic specification
     * @param state the {@link IState}
     * @param messages the messages
     * @param constraints the unsolved constraints
     * @param delays the delays
     * @param existentials the existentials; or {@code null}
     * @param completeness the completeness
     * @param placeholderDescriptors a map from meta variables to placeholder descriptors
     */
    protected RRSolverState(
        Spec spec,
        IState.Immutable state,
        Map.Immutable<IConstraint, IMessage> messages,
        Set.Immutable<IConstraint> constraints,
        Map.Immutable<IConstraint, Delay> delays,
        @Nullable ImmutableMap<ITermVar, ITermVar> existentials,
        ICompleteness.Immutable completeness,
        Map.Immutable<ITermVar, RRPlaceholderDescriptor> placeholderDescriptors
    ) {
        super(spec, state, messages, constraints, delays, existentials, completeness);
        this.placeholderDescriptors = placeholderDescriptors;
    }

    /**
     * Gets the map from constraint variables to placeholder descriptors.
     */
    public Map.Immutable<ITermVar, RRPlaceholderDescriptor> getPlaceholderDescriptors() {
        return this.placeholderDescriptors;
    }

    /**
     * Creates a copy of this {@link ISolverState} with the specified
     * placeholder descriptors.
     *
     * @param newPlaceholderDescriptors the new placeholder descriptors
     * @return the modified copy of the solver state
     */
    public RRSolverState withPlaceholderDescriptors(Map.Immutable<ITermVar, RRPlaceholderDescriptor> newPlaceholderDescriptors) {
        return copy(this.spec, this.state, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, newPlaceholderDescriptors);
    }

    /**
     * Creates a copy of this {@link ISolverState} with the specified
     * placeholder descriptor and variable mapping.
     *
     * @param metaVar the variable
     * @param placeholderDescriptor the descriptor
     * @return the modified copy of the solver state
     */
    public RRSolverState addPlaceholder(ITermVar metaVar, RRPlaceholderDescriptor placeholderDescriptor) {
        return withPlaceholderDescriptors(this.placeholderDescriptors.__put(metaVar, placeholderDescriptor));
    }

    @Override public RRSolverState withExistentials(Iterable<ITermVar> existentials) {
        return (RRSolverState)super.withExistentials(existentials);
    }

    @Override public RRSolverState withSingleConstraint() {
        return (RRSolverState)super.withSingleConstraint();
    }

    @Override public RRSolverState withPrecomputedCriticalEdges() {
        return (RRSolverState)super.withPrecomputedCriticalEdges();
    }

    @Override public RRSolverState withApplyResult(ApplyResult result, @Nullable IConstraint focus) {
        return (RRSolverState)super.withApplyResult(result, focus);
    }

    @Override public RRSolverState withUpdatedConstraints(
        java.util.Set<IConstraint> addConstraints,
        java.util.Set<IConstraint> removeConstraints
    ) {
        return (RRSolverState)super.withUpdatedConstraints(addConstraints, removeConstraints);
    }

    @Override public RRSolverState withState(IState.Immutable newState) {
        return (RRSolverState)super.withState(newState);
    }

    @Override public RRSolverState withUpdatedConstraints(Iterable<IConstraint> add, Iterable<IConstraint> remove) {
        return (RRSolverState)super.withUpdatedConstraints(add, remove);
    }

    @Override public RRSolverState withDelays(Iterable<? extends java.util.Map.Entry<IConstraint, Delay>> delays) {
        return (RRSolverState)super.withDelays(delays);
    }

    @Override public RRSolverState withDelay(IConstraint constraint, Delay delay) {
        return (RRSolverState)super.withDelay(constraint, delay);
    }

    @Override protected RRSolverState copy(
        Spec newSpec,
        IState.Immutable newState,
        Map.Immutable<IConstraint, IMessage> newMessages,
        Set.Immutable<IConstraint> newConstraints,
        Map.Immutable<IConstraint, Delay> newDelays,
        @Nullable ImmutableMap<ITermVar, ITermVar> newExistentials,
        ICompleteness.Immutable newCompleteness
    ) {
        return copy(newSpec, newState, newMessages, newConstraints, newDelays,
            newExistentials, newCompleteness, this.placeholderDescriptors);
    }

    /**
     * Creates a copy of this {@link RRSolverState} with the specified values.
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
     * @param newPlaceholderDescriptors the new placeholder descriptors
     * @return the modified copy of the {@link RRSolverState}
     */
    protected RRSolverState copy(
        Spec newSpec,
        IState.Immutable newState,
        Map.Immutable<IConstraint, IMessage> newMessages,
        Set.Immutable<IConstraint> newConstraints,
        Map.Immutable<IConstraint, Delay> newDelays,
        @Nullable ImmutableMap<ITermVar, ITermVar> newExistentials,
        ICompleteness.Immutable newCompleteness,
        Map.Immutable<ITermVar, RRPlaceholderDescriptor>  newPlaceholderDescriptors
    ) {
        return new RRSolverState(newSpec, newState, newMessages, newConstraints, newDelays,
            newExistentials, newCompleteness, newPlaceholderDescriptors);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final RRSolverState that = (RRSolverState)o;
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
    protected boolean safeEquals(RRSolverState that) {
        // @formatter:off
        return super.safeEquals(that)
            && Objects.equals(this.placeholderDescriptors, that.placeholderDescriptors);
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            super.hashCode(),
            this.placeholderDescriptors
            // NOTE: For the purposes of equality, we ignore the meta field
        );
    }

    @Override public void write(TextStringBuilder sb, String linePrefix, Function2<ITerm, IUniDisunifier, String> prettyPrinter) {
        super.write(sb, linePrefix, prettyPrinter);

        sb.append(linePrefix).append("placeholderDescriptors: ").appendln(placeholderDescriptors);
    }
}
