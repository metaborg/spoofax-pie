package mb.statix;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.substitution.ISubstitution;
import mb.nabl2.terms.substitution.PersistentSubstitution;
import mb.nabl2.terms.unification.UnifierFormatter;
import mb.nabl2.terms.unification.Unifiers;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.statix.constraints.CConj;
import mb.statix.constraints.CExists;
import mb.statix.constraints.messages.IMessage;
import mb.statix.constraints.messages.MessageKind;
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
import mb.statix.utils.TextStringBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.collection.CapsuleUtil;
import org.metaborg.util.functions.Function0;
import org.metaborg.util.functions.Function1;
import org.metaborg.util.functions.Function2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.tuple.Tuple2;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * The state of the solver.
 */
public class SolverState implements ISolverState {

    private final static ILogger log = LoggerUtils.logger(SolverState.class);

    /**
     * Creates a new {@link SolverState} from the given specification, solver state, and constraints.
     *
     * @param spec the semantic specification
     * @param state the solver state
     * @param constraints the constraints
     * @return the resulting search state
     */
    public static SolverState of(
        Spec spec,
        IState.Immutable state,
        Iterable<? extends IConstraint> constraints,
        Set.Immutable<String> expanded,
        SolutionMeta meta
    ) {
        final ICompleteness.Transient completeness = Completeness.Transient.of();
        completeness.addAll(constraints, spec, state.unifier());

        return new SolverState(spec, state, Map.Immutable.of(), CapsuleUtil.toSet(constraints), Map.Immutable.of(),
            null, completeness.freeze(), expanded, meta);
    }

    /**
     * Creates a new {@link SolverState} from the given solver result.
     *
     * @param result the result of inference by the solver
     * @param existentials the new map relating existentially quantified variables with their fresh variable;
     * or {@code null} to use the existing map from the solver result
     * @param expanded the new set of names of expanded rules
     * @param meta the new meta data
     * @return the resulting search state
     */
    public static SolverState fromSolverResult(
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
        return new SolverState(result.spec(), result.state(), CapsuleUtil.toMap(result.messages()),
            constraints.freeze(), delays.freeze(), newExistentials,
            result.completeness(), expanded, meta);
    }

    protected final Spec spec;
    protected final IState.Immutable state;
    protected final Set.Immutable<IConstraint> constraints;
    protected final Map.Immutable<IConstraint, Delay> delays;
    @Nullable protected final ImmutableMap<ITermVar, ITermVar> existentials;
    protected final ICompleteness.Immutable completeness;
    protected final Map.Immutable<IConstraint, IMessage> messages;
    protected final Set.Immutable<String> expanded;
    protected final SolutionMeta meta;

    /**
     * Initializes a new instance of the {@link SolverState} class.
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
    protected SolverState(
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
        this.spec = spec;
        this.state = state;
        this.messages = messages;
        this.constraints = constraints;
        this.delays = delays;
        this.existentials = existentials;
        this.completeness = completeness;
        this.expanded = expanded;
        this.meta = meta;
    }

    @Override public Spec getSpec() {
        return this.spec;
    }

    @Override public IState.Immutable getState() {
        return this.state;
    }

    @Override public Map.Immutable<IConstraint, IMessage> getMessages() {
        return this.messages;
    }

    @Override public Set.Immutable<IConstraint> getConstraints() {
        return this.constraints;
    }

    @Override public Map.Immutable<IConstraint, Delay> getDelays() {
        return this.delays;
    }

    @Override @Nullable public ImmutableMap<ITermVar, ITermVar> getExistentials() {
        return this.existentials;
    }

    @Override public ICompleteness.Immutable getCompleteness() {
        return this.completeness;
    }

    @Override public Set.Immutable<String> getExpanded() {
        return this.expanded;
    }

    @Override public SolutionMeta getMeta() {
        return this.meta;
    }

    @Override public SolverState withExpanded(Set.Immutable<String> newExpanded) {
        return copy(this.spec, this.state, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, newExpanded, this.meta);
    }

    @Override public SolverState withExistentials(Iterable<ITermVar> existentials) {
        // We wrap all constraints in a conjunction,
        // and wrap the result in an existential constraint.
        SolverState newState = withSingleConstraint();
        assert newState.constraints.size() <= 1;
        if (newState.constraints.isEmpty()) {
            // No constraints, so what can you do? ¯\_(ツ)_/¯
            return this;
        }
        final IConstraint constraint = this.constraints.iterator().next();
        final IConstraint newConstraint = new CExists(existentials, constraint);
        return copy(this.spec, this.state, this.messages, Set.Immutable.of(newConstraint), this.delays,
            // NOTE: we discard any previous existentials
            null, this.completeness, this.expanded, meta);
    }

    @Override public SolverState withSingleConstraint() {
        if (this.constraints.size() <= 1) return this;
        Iterator<IConstraint> iterator = this.constraints.iterator();
        // We wrap all constraints in a conjunction.
        IConstraint newConstraint = iterator.next();
        while(iterator.hasNext()) {
            newConstraint = new CConj(newConstraint, iterator.next());
        }
        return copy(this.spec, this.state, this.messages, Set.Immutable.of(newConstraint), this.delays,
            this.existentials, this.completeness, this.expanded, meta);
    }

    @Override public SolverState withPrecomputedCriticalEdges() {
        SolverState newState = withSingleConstraint();
        assert newState.constraints.size() <= 1;
        if (newState.constraints.isEmpty()) {
            // No constraints, so what can you do? ¯\_(ツ)_/¯
            return this;
        }
        final IConstraint constraint = newState.constraints.iterator().next();
        final Tuple2<IConstraint, ICompleteness.Immutable> result =
            CompletenessUtil.precomputeCriticalEdges(constraint, spec.scopeExtensions());
        final IConstraint newConstraint = result._1();
        ICompleteness.Transient completeness = this.completeness.melt();
        completeness.addAll(result._2(), state.unifier());
        return copy(this.spec, this.state, this.messages, Set.Immutable.of(newConstraint), this.delays,
            this.existentials, completeness.freeze(), this.expanded, meta);
    }

    @Override public SolverState withApplyResult(ApplyResult result, @Nullable IConstraint focus) {
        final IConstraint applyConstraint = result.body();
        final IState.Immutable applyState = this.state;
        final IUniDisunifier.Immutable applyUnifier = applyState.unifier();

        // Update constraints
        final Set.Transient<IConstraint> constraints = this.getConstraints().asTransient();
        constraints.__insert(applyConstraint);
        if (focus != null) constraints.__remove(focus);

        // Update completeness
        final ICompleteness.Transient completeness = this.getCompleteness().melt();
        completeness.add(applyConstraint, spec, applyUnifier);
        final java.util.Set<CriticalEdge> removedEdges;
        if (focus != null)
            removedEdges = completeness.remove(focus, spec, applyUnifier);
        else
            removedEdges = Collections.emptySet();

        // Update delays
        final Map.Transient<IConstraint, Delay> delays = Map.Transient.of();
        this.getDelays().forEach((c, d) -> {
            if(!Sets.intersection(d.criticalEdges(), removedEdges).isEmpty()) {
                constraints.__insert(c);
            } else {
                delays.__put(c, d);
            }
        });

        return copy(this.spec, applyState, this.messages, constraints.freeze(), delays.freeze(),
            this.existentials, completeness.freeze(), expanded, meta);
    }

    @Override public SolverState withState(IState.Immutable newState) {
        return copy(this.spec, newState, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, this.expanded, this.meta);
    }

    @Override public SolverState withUpdatedConstraints(Iterable<IConstraint> add, Iterable<IConstraint> remove) {
        final ICompleteness.Transient completeness = this.completeness.melt();
        final Set.Transient<IConstraint> constraints = this.constraints.asTransient();
        final java.util.Set<CriticalEdge> removedEdges = Sets.newHashSet();
        add.forEach(c -> {
            if(constraints.__insert(c)) {
                completeness.add(c, spec, state.unifier());
            }
        });
        remove.forEach(c -> {
            if(constraints.__remove(c)) {
                removedEdges.addAll(completeness.remove(c, spec, state.unifier()));
            }
        });
        final Map.Transient<IConstraint, Delay> delays = Map.Transient.of();
        this.delays.forEach((c, d) -> {
            if(!Sets.intersection(d.criticalEdges(), removedEdges).isEmpty()) {
                constraints.__insert(c);
            } else {
                delays.__put(c, d);
            }
        });
        return copy(this.spec, this.state, this.messages, constraints.freeze(), delays.freeze(),
            this.existentials, completeness.freeze(), this.expanded, this.meta);
    }

    @Override public SolverState withDelays(Iterable<? extends java.util.Map.Entry<IConstraint, Delay>> delays) {
        final Set.Transient<IConstraint> constraints = this.constraints.asTransient();
        final Map.Transient<IConstraint, Delay> newDelays = this.delays.asTransient();
        delays.forEach(entry -> {
            if(constraints.__remove(entry.getKey())) {
                newDelays.__put(entry.getKey(), entry.getValue());
            } else {
                log.warn("delayed constraint not in constraint set: {}", entry.getKey());
            }
        });
        return copy(this.spec, this.state, this.messages, constraints.freeze(), newDelays.freeze(),
            this.existentials, this.completeness, this.expanded, this.meta);
    }

    @Override public SolverState withMeta(SolutionMeta newMeta) {
        return copy(this.spec, this.state, this.messages, this.constraints, this.delays,
            this.existentials, this.completeness, this.expanded, newMeta);
    }

    @Override public <C extends IConstraint> SelectedConstraintSolverState<C> withSelected(C selection) {
        return SelectedConstraintSolverState.of(selection, this);
    }

    @Override public SolverState withoutSelected() {
        return this;
    }

    /**
     * Creates a copy of this {@link SolverState} with the specified values.
     *
     * This method should only invoke the constructor.
     *
     * This method can be overridden in subclasses to invoke the subclass constructor instead.
     *
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
    protected SolverState copy(
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
        return new SolverState(newSpec, newState, newMessages, newConstraints, newDelays,
            newExistentials, newCompleteness, newExpanded, newMeta);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SolverState that = (SolverState)o;
        return this.safeEquals(that);
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
    protected boolean safeEquals(SolverState that) {
        // NOTE: For the purposes of equality, we ignore the metadata
        // @formatter:off
        return Objects.equals(this.spec, that.spec)
            && Objects.equals(this.state, that.state)
            && Objects.equals(this.constraints, that.constraints)
            && Objects.equals(this.delays, that.delays)
            && Objects.equals(this.existentials, that.existentials)
            && Objects.equals(this.completeness, that.completeness)
            && Objects.equals(this.messages, that.messages)
            && Objects.equals(this.expanded, that.expanded);
        // @formatter:on
    }

    @Override
    public int hashCode() {
        // NOTE: For the purposes of equality, we ignore the metadata
        return Objects.hash(
            this.spec,
            this.state,
            this.constraints,
            this.delays,
            this.existentials,
            this.completeness,
            this.messages,
            this.expanded
        );
    }

    @Override public String toString() {
        final TextStringBuilder sb = new TextStringBuilder();
        sb.append(this.getClass().getSimpleName()).appendln(":");
        write(sb, "| ", (t, u) -> new UnifierFormatter(u, /* Increased from 2 */ Integer.MAX_VALUE).format(t));
        return sb.toString();
    }

    @Override public void write(TextStringBuilder sb, String linePrefix, Function2<ITerm, IUniDisunifier, String> prettyPrinter) {
        final IUniDisunifier unifier = state.unifier();
        if (existentials != null) {
            sb.append(linePrefix).appendln("vars:");
            for (Map.Entry<ITermVar, ITermVar> existential : existentials.entrySet()) {
                String var = prettyPrinter.apply(existential.getKey(), Unifiers.Immutable.of());
                String term = prettyPrinter.apply(existential.getValue(), unifier);
                sb.append(linePrefix).append("  ").append(var).append(" : ").append(term).append(" = ").appendln(project(existential.getValue()));
            }
        } else {
            sb.append(linePrefix).appendln("vars: <null>");
        }
        sb.append(linePrefix).append("unifier: ").appendln(state.unifier());
        if (!completeness.isEmpty()) {
            sb.append(linePrefix).append("completeness: ").appendln(completeness);
        }
        sb.append(linePrefix).appendln("constraints:");
        for (IConstraint c : constraints) {
            sb.append(linePrefix).append("  ").appendln(c.toString(t -> prettyPrinter.apply(t, unifier)));
        }
        if (!delays.entrySet().isEmpty()) {
            sb.append(linePrefix).appendln("delays:");
            for(java.util.Map.Entry<IConstraint, Delay> e : delays.entrySet()) {
                sb.append(linePrefix).append("  ").append(e.getValue()).append(" : ").appendln(e.getKey().toString(t -> prettyPrinter.apply(t, unifier)));
            }
        }

        sb.append(linePrefix).appendln("meta:");
        sb.append(linePrefix).append("  expandedQueries: ").appendln(meta.getExpandedQueries());
        sb.append(linePrefix).append("  expandedRules: ").appendln(meta.getExpandedRules());

        writeMessages(sb, linePrefix, prettyPrinter);
    }

    /**
     * Writes a summary of the messages of the solver state to the specified string builder.
     *
     * @param sb the writer to write to
     * @param linePrefix the line prefix to use
     * @param prettyPrinter a function that, given a term and a unifier-disunifier,
     *                      produces a string representation of the term.
     */
    private void writeMessages(TextStringBuilder sb, String linePrefix, Function2<ITerm, IUniDisunifier, String> prettyPrinter) {
        final IUniDisunifier unifier = state.unifier();

        final Function0<String> defaultMessage = () -> "<empty>";
        final Function1<ICompleteness.Immutable, String> formatCompleteness = completeness -> {
            final ISubstitution.Transient subst = PersistentSubstitution.Transient.of();
            completeness.vars().forEach(var -> {
                ITerm sub = unifier.findRecursive(var);
                if(!sub.equals(var)) {
                    subst.put(var, sub);
                }
            });
            return completeness.apply(subst.freeze()).toString();
        };

        List<java.util.Map.Entry<IConstraint, IMessage>> errors = messages.entrySet().stream().filter(it -> it.getValue().kind() == MessageKind.ERROR).collect(Collectors.toList());
        if (!errors.isEmpty()) {
            sb.append(linePrefix).appendln("errors:");
            for(java.util.Map.Entry<IConstraint, IMessage> e : errors) {
                sb.append(linePrefix).append("  - ").appendln(e.getValue().toString(ITerm::toString, defaultMessage, formatCompleteness));
                sb.append(linePrefix).append("    ").appendln(e.getKey().toString(t -> prettyPrinter.apply(t, unifier)));
            }
        }
        List<java.util.Map.Entry<IConstraint, IMessage>> warnings = messages.entrySet().stream().filter(it -> it.getValue().kind() == MessageKind.WARNING).collect(Collectors.toList());
        if (!warnings.isEmpty()) {
            sb.append(linePrefix).appendln("warnings:");
            for(java.util.Map.Entry<IConstraint, IMessage> e : warnings) {
                sb.append(linePrefix).append("  - ").appendln(e.getValue().toString(ITerm::toString, defaultMessage, formatCompleteness));
                sb.append(linePrefix).append("    ").appendln(e.getKey().toString(t -> prettyPrinter.apply(t, unifier)));
            }
        }
        List<java.util.Map.Entry<IConstraint, IMessage>> notes = messages.entrySet().stream().filter(it -> it.getValue().kind() == MessageKind.NOTE).collect(Collectors.toList());
        if (!notes.isEmpty()) {
            sb.append(linePrefix).appendln("notes:");
            for(java.util.Map.Entry<IConstraint, IMessage> e : notes) {
                sb.append(linePrefix).append("  - ").appendln(e.getValue().toString(ITerm::toString, defaultMessage, formatCompleteness));
                sb.append(linePrefix).append("    ").appendln(e.getKey().toString(t -> prettyPrinter.apply(t, unifier)));
            }
        }
    }
}
