package mb.statix.codecompletion;

import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.Terms;
import mb.nabl2.terms.build.TermBuild;
import mb.nabl2.terms.matching.TermPattern;
import mb.nabl2.terms.stratego.PlaceholderVarMap;
import mb.nabl2.terms.stratego.StrategoPlaceholders;
import mb.nabl2.terms.unification.OccursException;
import mb.nabl2.terms.unification.Unifiers;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.nabl2.terms.substitution.ISubstitution;
import mb.nabl2.terms.substitution.PersistentSubstitution;
import mb.statix.CodeCompletionProposal;
import mb.statix.SolverState;
import mb.statix.solver.IState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An incomplete AST,
 * and a mapping from term variables to their expected ASTs.
 */
@Value.Immutable(builder = false)
abstract class AbstractCompletionExpectation<T extends ITerm> {

    public static CompletionExpectation<? extends ITerm> fromTerm(ITerm incompleteTerm, ITerm completeTerm, PlaceholderVarMap placeholderVarMap) {
        // Gather all the placeholders in the term
        ITerm replacedTerm = StrategoPlaceholders.replacePlaceholdersByVariables(incompleteTerm, placeholderVarMap);
        // Does the term we got, including variables, match the expected term?
        Optional<ISubstitution.Immutable> optSubstitution = TermPattern.P.fromTerm(replacedTerm).match(completeTerm);
        if (!optSubstitution.isPresent()) throw new IllegalStateException("The incomplete term is not a match to the complete term.");
        // Yes, and the substitution shows the new variables and their expected term values
        ISubstitution.Immutable substitution = optSubstitution.get();
        HashMap<ITermVar, ITerm> expectedAsts = new HashMap<>();
        for(Map.Entry<ITermVar, ITerm> entry : substitution.entrySet()) {
            expectedAsts.put(entry.getKey(), entry.getValue());
        }
        return CompletionExpectation.of(replacedTerm, expectedAsts, null);
    }

    /**
     * Gets the AST that is being built.
     *
     * @return the incomplete AST
     */
    @Value.Parameter public abstract T getIncompleteAst();

    /**
     * Gets the expected values for the various placeholders in the term.
     *
     * @return the expectations
     */
    @Value.Parameter public abstract Map<ITermVar, ITerm> getExpectations();

    /**
     * Gets the solver state of the incomplete AST.
     *
     * @return the solver state
     */
    @Value.Parameter public abstract @Nullable SolverState getState();

    /**
     * Gets the set of term variables for which we need to find completions.
     *
     * @return the set of term variables; or an empty set when completion is done
     */
    @Value.Derived public Set<ITermVar> getVars() {
        return getExpectations().keySet();
    }

    /**
     * Whether the AST is complete.
     *
     * @return {@code true} when the AST is complete; otherwise, {@code false}
     */
    @Value.Derived public boolean isComplete() {
        return getVars().isEmpty();
    }

    /**
     * Replaces the specified term variable with the specified term,
     * if it is the term that we expected.
     *
     * @param var the term variable to replace
     * @param proposal the proposal to replace it with, which may contain term variables
     * @return the resulting incomplete AST if replacement succeeded; otherwise, {@code null} when it doesn't fit
     */
    public @Nullable CompletionExpectation<? extends ITerm> tryReplace(ITermVar var, CodeCompletionProposal proposal) {
        ITerm term = proposal.getTerm();
        if (var.equals(term)) {
            // Trying to replace by the same variable indicates that the proposal
            // did not replace the variable by a term.
            return null;
        }

        SolverState newState = proposal.getState();

        ISubstitution.@Nullable Immutable substitution = trySubtitute(var, term);
        if (substitution == null) {
            // The variable can never be replaced by the actual term,
            // so we reject this proposal.
            return null;
        }

        IUniDisunifier.@Nullable Immutable expectedUnifier = tryUnify(proposal.getState().getState().unifier(), this.getExpectations().entrySet());
        if (expectedUnifier == null) {
            // The expectations cannot unify with the current unifier,
            // so we reject this proposal.
            return null;
        }

        IUniDisunifier.@Nullable Immutable expectedUnifier2 = tryUnify(expectedUnifier, substitution.entrySet());
        if (expectedUnifier2 == null) {
            // The substitution cannot unify with the current unifier,
            // so we reject this proposal.
            return null;
        }

        // Additionally, we can only accept a proposal if the other variables can be matched to their new values,
        // or where the new value is the same as the old value, the new value is a variable, or the new value is unknown.
        for (ITermVar v : this.getVars()) {
            if (v.equals(var)) continue;
            ITerm actualTerm = newState.project(v);
            boolean matches = trySubtitute(v, actualTerm) != null;
            if (!matches) {
                // The variable can never be replaced by the value in the unifier,
                // so we reject this proposal.
                return null;
            }
        }

        // The substitution shows the new variables and their expected term values
        HashMap<ITermVar, ITerm> expectedAsts = new HashMap<>(this.getExpectations());
        expectedAsts.remove(var);
        for(Map.Entry<ITermVar, ITerm> entry : substitution.entrySet()) {
            expectedAsts.compute(entry.getKey(), (k, v) -> {
                if (v != null && !v.equals(entry.getValue()))
                    throw new IllegalStateException("Trying to add expectation " + k + " |-> " + entry.getValue() + ", but already has expectation |-> " + v + ".");
                return entry.getValue();
            });
            expectedAsts.put(entry.getKey(), entry.getValue());
        }
        ITerm newIncompleteAst = PersistentSubstitution.Immutable.of(var, term).apply(getIncompleteAst());
        return CompletionExpectation.of(newIncompleteAst, expectedAsts, newState);
    }

    public ISubstitution.@Nullable Immutable trySubtitute(ITermVar var, ITerm actualTerm) {
        ITerm expectedTerm = getExpectations().get(var);
        assert expectedTerm != null;
        // Does the term we got, including variables, match the expected term?
        return TermPattern.P.fromTerm(actualTerm).match(expectedTerm).orElse(null);
    }

    public IUniDisunifier.@Nullable Immutable tryUnify(IUniDisunifier.Immutable unifier, Iterable<Map.Entry<ITermVar, ITerm>> pairs) {
        try {
            return unifier.unify(pairs).map(IUniDisunifier.Result::unifier).orElse(null);
        } catch(OccursException e) {
            return null;
        }
    }

    @SuppressWarnings("Convert2MethodRef")
    private static ITerm makeVarsFresh(ITerm term, IState.Transient state) {
        return term.match(Terms.cases(
            appl -> TermBuild.B.newAppl(appl.getOp(), appl.getArgs().stream().map(a -> makeVarsFresh(a, state)).collect(Collectors.toList()), appl.getAttachments()),
            list -> makeVarsFreshInList(list, state),
            string -> string,
            integer -> integer,
            blob -> blob,
            var -> freshVar(var, state)
        ));
    }

    @SuppressWarnings("Convert2MethodRef")
    private static IListTerm makeVarsFreshInList(IListTerm term, IState.Transient state) {
        return term.match(ListTerms.cases(
            cons -> TermBuild.B.newCons(makeVarsFresh(cons.getHead(), state), makeVarsFreshInList(cons.getTail(), state), cons.getAttachments()),
            nil -> nil,
            var -> freshVar(var, state)
        ));
    }

    private static ITermVar freshVar(ITermVar var, IState.Transient state) {
        return state.freshVar(var);
    }
}
