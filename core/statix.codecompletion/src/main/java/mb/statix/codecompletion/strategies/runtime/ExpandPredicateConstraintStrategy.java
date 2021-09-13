package mb.statix.codecompletion.strategies.runtime;

import com.google.common.collect.ImmutableList;
import io.usethesource.capsule.Set;
import mb.nabl2.terms.ITermVar;
import mb.statix.SelectedConstraintSolverState;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.constraints.CUser;
import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.spec.ApplyMode;
import mb.statix.spec.ApplyResult;
import mb.statix.spec.Rule;
import mb.statix.spec.RuleUtil;
import mb.statix.strategies.NamedStrategy1;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Given a state with a selected predicate constraint {@link CUser},
 * this strategy expands the initial state into as many states as there are rules defined for the predicate.
 */
public final class ExpandPredicateConstraintStrategy extends NamedStrategy1<SolverContext, ITermVar, SelectedConstraintSolverState<CUser>, SolverState> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final ExpandPredicateConstraintStrategy instance = new ExpandPredicateConstraintStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static ExpandPredicateConstraintStrategy getInstance() { return (ExpandPredicateConstraintStrategy)instance; }

    private ExpandPredicateConstraintStrategy() {}

    @Override
    public String getName() {
        return "expandPredicateConstraint";
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "focus";
            default: return super.getParamName(index);
        }
    }

    /**
     * Executes the strategy.
     *
     * @param ctx the context
     * @param focus the focus, used for debugging; or {@code null}
     * @param state the solver state with a selected constraint
     * @return the sequence of new solver states
     */
    @Override
    public Seq<SolverState> eval(SolverContext ctx, @Nullable ITermVar focus, SelectedConstraintSolverState<CUser> state) {
        final CUser selected = state.getSelected();
        // Get the rules for the given predicate constraint
        final ImmutableList<Rule> rules = ctx.getSpec().rules().getOrderIndependentRules(selected.name()).asList();
        // Prepare the new state for each of the expansions:
        // - Remove the selected constraint
        // - Add the constraint's name to the set of expanded constraints
        // - Increment the number of expanded rules
        final SolverState newState = state
            .withoutSelected()
            .withExpanded(addToSet(state.getExpanded(), selected.name()))
            .withMeta(state.getMeta().withExpandedRulesIncremented());

        return applyAllLazy(rules, selected, newState);
    }

    /**
     * Adds an element to an immutable set, by creating a new set.
     *
     * @param set the immutable set
     * @param element the element to add
     * @return the new immutable set
     */
    private static Set.Immutable<String> addToSet(Set.Immutable<String> set, String element) {
        Set.Transient<String> transientSet = set.asTransient();
        transientSet.__insert(element);
        return transientSet.freeze();
    }

    /**
     * Applies the given rules to the arguments of the selected constraint, eagerly.
     *
     * @param rules the rules to apply
     * @param selected the selected constraint
     * @param state the initial state
     * @return a sequence of new states
     */
    @SuppressWarnings("unused")
    private static Seq<SolverState> applyAllEager(
        List<Rule> rules,
        CUser selected,
        SolverState state
    ) {
        final List<SolverState> output = RuleUtil.applyAll(state.getState().unifier(), rules, selected.args(), selected,
                ApplyMode.RELAXED, ApplyMode.Safety.UNSAFE).stream()
            .map(t -> state.withApplyResult(t._2(), selected))
            .collect(Collectors.toList());
        return Seq.from(output);
    }

    /**
     * Applies the given rules to the arguments of the selected constraint, lazily.
     *
     * @param rules the rules to apply
     * @param selected the selected constraint
     * @param state the initial state
     * @return a sequence of new states
     */
    @SuppressWarnings("unused")
    private static Seq<SolverState> applyAllLazy(
        List<Rule> rules,
        CUser selected,
        SolverState state
    ) {
        return new SeqBase<SolverState>() {
            private int index = 0;
            @Override
            protected void computeNext() {
                @Nullable ApplyResult result;
                do {
                    if (index >= rules.size()) {
                        yieldBreak();
                        return;
                    }
                    final Rule rule = rules.get(index);
                    result = RuleUtil.apply(state.getState().unifier(), rule, selected.args(), selected,
                        ApplyMode.RELAXED, ApplyMode.Safety.UNSAFE).orElse(null);
                    index += 1;
                } while (result == null);
                final SolverState resultState = state.withApplyResult(result, selected);
                this.yield(resultState);
            }
        };
    }

}
