package mb.statix.referenceretention.tego;

import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.IStringTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.stratego.TermIndex;
import mb.statix.constraints.CEqual;
import mb.statix.referenceretention.statix.RRLockedReference;
import mb.statix.referenceretention.statix.RRPlaceholder;
import mb.statix.solver.ITermProperty;
import mb.statix.solver.persistent.SolverResult;
import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy3;
import mb.tego.strategies.Strategy2;
import mb.tego.strategies.Strategy3;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.tuples.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.tuple.Tuple2;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * If the placeholder is a locked reference and has no context, this verifies that the reference still resolves
 * to the locked reference's declaration. Otherwise, if the placeholder is a locked reference with a context,
 * this creates alternatives where the name is qualified and unqualified and the placeholder has no context.
 * Otherwise, if the placeholder is a term, this unwraps the term.
 */
public final class UnwrapOrFixReferenceStrategy extends NamedStrategy3<RRContext, ITermVar, RRPlaceholder, RRSolverState, Seq<RRSolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final UnwrapOrFixReferenceStrategy instance = new UnwrapOrFixReferenceStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static UnwrapOrFixReferenceStrategy getInstance() { return (UnwrapOrFixReferenceStrategy)instance; }

    private UnwrapOrFixReferenceStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "unwrapOrFixReference";
    }

    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "RedundantSuppression"})
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "ctx";
            case 1: return "v";
            case 2: return "descriptor";
            default: return super.getParamName(index);
        }
    }

    @Override
    public Seq<RRSolverState> evalInternal(
        TegoEngine engine,
        RRContext ctx,
        ITermVar v,
        RRPlaceholder descriptor,
        RRSolverState input
    ) {
        return eval(engine, ctx, v, descriptor, input);
    }

    /**
     * Unwrap or fix a placeholder reference.
     *
     * @param engine the Tego engine
     * @param ctx the solver context
     * @param v the placeholder variable
     * @param descriptor the descriptor for the placeholder
     * @param input the input solver state
     * @return a lazy sequence of solver states
     */
    public static Seq<RRSolverState> eval(
        TegoEngine engine,
        RRContext ctx,
        ITermVar v,
        RRPlaceholder descriptor,
        RRSolverState input
    ) {
        // Construct the state without the placeholder being unwrapped/fixed
        final RRSolverState inputWithoutPlaceholder = input.removePlaceholder(v);

        // (body, contexts) <- descriptor,
        final ITerm term = descriptor.getBody();
        final List<ITerm> contextTerms = descriptor.getContextTerms();

        final @Nullable RRLockedReference lockedRef = RRLockedReference.matcher().match(term).orElse(null);
        if (lockedRef != null) {
            // A locked reference
            // ⟦ r | C ⟧ = { R } when O(C, r) => R and r' in R |-> d
            return qualifyReference(
                engine,
                ctx,
                contextTerms,
                lockedRef,
                inputWithoutPlaceholder,
                v
            );
//            // The term is a locked reference
//            if (contextTerms.size() == 0) {
//                // A locked reference to declaration d with no context.
//                // Check whether the resulting reference resolves to declaration d
//                // ⟦ r^d | ε ⟧ = { r } when r |-> d else {}
//                // Apply the context to the reference r^d and check whether the result is a valid locked reference to declaration d
//                // Or should we do the check somewhere else?
//                // TODO: Do the check whether the reference still points to the given declaration
//
//                final ITerm referenceTerm = lockedRef.getTerm();
//                final TermIndex referenceIndex = checkNotNull(tryGetTermIndex(input, referenceTerm), "Reference term has no index: " + referenceTerm);
//
//                final RRSolverState testState = bindToVar(inputWithoutPlaceholder, v, lockedRef.getTerm());
//                @Nullable final RRSolverState testResultState = engine.eval(InferStrategy.getInstance(), testState);
//
//                if (testResultState == null || testResultState.hasErrors()) return Seq.of();   // TODO: Report that it has errors and was therefore excluded
//                final ITerm refTargetTerm = checkNotNull(tryGetRefProperty(testResultState, referenceIndex), "Reference has no @ref target: " + referenceTerm);
//                final TermIndex refTargetIndex = checkNotNull(tryGetTermIndex(testResultState, refTargetTerm), "Reference target has no index: " + refTargetTerm);
//
//                if (refTargetIndex.equals(lockedRef.getDeclaration())) {
//                    // Check succeeded, reference is valid.
//                    final RRSolverState newState = bindToVar(inputWithoutPlaceholder, v, lockedRef.getTerm());
//                    return Seq.of(newState);
//                } else {
//                    // Check failed, reference is invalid.
//                    return Seq.of();
//                }
//            } else if (contextTerms.size() == 1) {
//                // A locked reference to declaration d with a single context.
//                // Apply the context and create a placeholder with the new reference but without a context.
//                // ⟦ r | c0 ⟧ = { ⟦ r' | ε ⟧ } where c0(r) == r' else {}
//                final ITerm context = contextTerms.get(0);
//                // This executes the context on the reference
//                final @Nullable ITerm qreference = engine.eval(qualifyReference, context, lockedRef.getTerm());
//                if(qreference != null) {
//                    final RRSolverState newState = unwrapAppl(engine, v, (IApplTerm)term, contextTerms, inputWithoutPlaceholder);
//                    return Seq.of(newState);
//                } else {
//                    // No results.
//                    return Seq.of();
//                }
//            } else {  // contexts.size() > 1
//                // A locked reference with multiple contexts, split into multiple states each with a single context
//                // ⟦ r | c0, c1, .. ⟧ = { ⟦ r | c0 ⟧, ⟦ r | c1 ⟧, .. }
//                List<RRSolverState> newStates = new ArrayList<>(contextTerms.size() + 1);
//                for(ITerm context : contextTerms) {
//                    final Pair<ITermVar, RRSolverState> varAndstate = newPlaceholder(engine, inputWithoutPlaceholder, lockedRef, Collections.singletonList(context));
//                    final ITermVar newVar = varAndstate.component1();
//                    final RRSolverState newState = varAndstate.component2();    // TODO: Do we need to apply `bindToVar`?
//                    newStates.add(newState);
//                }
//                return Seq.from(newStates);
//            }
        } else if (term instanceof IApplTerm) {
            // The term is a term application, so we unwrap it once
            //   ⟦ T(a0, a1, ..) | C ⟧  ->  { T( ⟦ a0 | C ⟧, ⟦ a1 | C ⟧, .. ) }
            final RRSolverState finalState = unwrapAppl(engine, v, (IApplTerm)term, contextTerms, inputWithoutPlaceholder);
            return Seq.of(finalState);
        } else if (term instanceof IListTerm) {
            // The term is a list term, so we unwrap it into its elements
            //   ⟦ [e0, e1, ..] | C ⟧  ->  { [ ⟦ e0 | C ⟧, ⟦ e1 | C ⟧, .. ] }
            final RRSolverState finalState = unwrapList(engine, v, (IListTerm)term, contextTerms, inputWithoutPlaceholder);
            return Seq.of(finalState);
        } else {
            // The term is not a term application and not a locked reference
            //   ⟦ t | _ ⟧  ->  { t }
            final RRSolverState newState = bindToVar(inputWithoutPlaceholder, v, term);
            return Seq.of(newState);
        }

        // Done!
    }

    /**
     * For a locked reference, determines all the possible qualified references and tests each to see if it
     * still resolves to the correct declaration (by term index).
     *
     * @param engine the Tego engine
     * @param ctx the reference retention context
     * @param contextTerms the context terms that were passed to the placeholder
     * @param lockedRef the locked reference
     * @param state the current solver state
     * @param v the variable for which the locked reference is a placeholder
     * @return the new solver states
     */
    private static Seq<RRSolverState> qualifyReference(
        TegoEngine engine,
        RRContext ctx,
        List<ITerm> contextTerms,
        RRLockedReference lockedRef,
        RRSolverState state,
        ITermVar v
    ) {
        // An oracle function that takes a context and a term,
        // and returns a list of pairs of a term and a term index.
        // The term index is the index of the final reference in the resulting term,
        // the one whose @ref property should point to the declaration.
        // For example, while inlining a call `c.f()`, the context is the term `c`
        // and the term is a reference to be fixed `x`. The result can be a term `c.x`,
        // a qualified reference, and the term index of `x`.
        final Strategy3</* ctx */ IListTerm, /* sortName */ IStringTerm, /* SolverResult */ SolverResult, /* term */ ITerm, /* result */ @Nullable ITerm> qualifyReference = ctx.getQualifyReferenceStrategy();

        final @Nullable ITerm qreferences = engine.eval(qualifyReference, B.newList(contextTerms), lockedRef.getSortName(), state.toSolverResult(), lockedRef.getTerm());
        if (qreferences == null) {
            return Seq.of(); // No qualified reference
        }
        // The result of the strategy should be a list of pairs of a term and a term index.
        final List<Pair<ITerm, TermIndex>> pairs = M.listElems(M.tuple2(M.term(), TermIndex.matcher(), (a, b, c) -> Pair.of(b, c))).match(qreferences).orElse(Collections.emptyList());
        // TODO: Error if the match failed
        final ArrayList<RRSolverState> newStates = new ArrayList<>(pairs.size());
        for (Pair<ITerm, TermIndex> pair : pairs) {
            // The qualified reference term generated by the oracle
            final ITerm qreference = pair.component1();
            // The reference target index, from which to find the @ref property
            final TermIndex refTargetIndex = pair.component2();
            final @Nullable RRSolverState newState = lockedReferenceToValidReference(
                engine,
                qreference,
                refTargetIndex,
                lockedRef.getDeclaration(),
                state,
                v
            );
            if (newState != null) {
                newStates.add(newState);
            } else {
                // TODO: Else, report invalid solver state?
                System.out.println("Rejected: " + qreference);
            }
        }
//
//        final List<RRSolverState> newStates = pairs.stream().map(pair -> {
//            final IApplTerm qreference = pair.component1();
//            final TermIndex refTargetIndex = pair.component2();
//            return unwrapAppl(engine, v, qreference, contextTerms, state);
//        }).collect(Collectors.toList());
        return Seq.fromIterable(newStates);
    }

    /**
     * For a locked reference, tests it to see if it
     * still resolves to the correct declaration (by term index).
     *
     * @param engine the Tego engine
     * @param referenceTerm the reference term (e.g., the qualified reference)
     * @param referenceIndex the index of the reference term on which to find the @ref property
     * @param declarationIndex the index of the declaration term expected to be found
     * @param state the current solver state
     * @param v the variable for which the locked reference is a placeholder
     * @return the new solver state; or {@code null} if the state was rejected
     */
    private static @Nullable RRSolverState lockedReferenceToValidReference(
        TegoEngine engine,
        ITerm referenceTerm,
        TermIndex referenceIndex,
        TermIndex declarationIndex,
        RRSolverState state,
        ITermVar v
    ) {

//        final ITerm referenceTerm = lockedRef.getTerm();
//        final TermIndex referenceIndex = checkNotNull(tryGetTermIndex(input, referenceTerm), "Reference term has no index: " + referenceTerm);

        final RRSolverState testState = bindToVar(state, v, referenceTerm);
        @Nullable final RRSolverState testResultState = engine.eval(InferStrategy.getInstance(), testState);

        if (testResultState == null || testResultState.hasErrors()) {
            engine.log(instance, "Locked reference test has errors, excluded: {} => {}", referenceTerm, testResultState);
            return null;   // TODO: Report that it has errors and was therefore excluded
        }
        final @Nullable ITerm refTargetTerm = tryGetRefProperty(testResultState, referenceIndex); //, "Reference has no @ref target: " + referenceTerm);
        if(refTargetTerm == null) {
            engine.log(instance, "Reference has no @ref target, excluded: {} in {}", referenceIndex, testResultState);
            return null;   // TODO: Report that reference had no @ref target and was therefore excluded
        }

        final @Nullable TermIndex refTargetIndex = tryGetTermIndex(testResultState, refTargetTerm); //, "Reference target has no index: " + refTargetTerm);
        if(refTargetIndex == null) {
            engine.log(instance, "Reference target has no index, excluded: {} in {}", refTargetTerm, testResultState);
            return null;   // TODO: Report that reference had no index and was therefore excluded
        }

        if (refTargetIndex.equals(declarationIndex)) {
            // Check succeeded, reference is valid.
//            final RRSolverState newState = bindToVar(inputWithoutPlaceholder, v, lockedRef.getTerm());
            return testResultState;
        } else {
            // Check failed, reference is invalid.
            engine.log(instance, "Locked reference test failed (points to different declaration), excluded: {} => {}", referenceTerm, testResultState);
            return null;
        }
    }

    /**
     * Attempts to get the term index of the specified term or term variable.
     *
     * @param state the solver state
     * @param term the term or term variable
     * @return the term index; or {@code null} if not found
     */
    private static @Nullable TermIndex tryGetTermIndex(RRSolverState state, ITerm term) {
        final ITerm projectedTerm = term instanceof ITermVar ? state.project((ITermVar)term) : term;
        return TermIndex.get(projectedTerm).orElse(null);
    }

    /**
     * Attempts to get the @ref property of the term with the specified term index.
     *
     * @param state the solver state
     * @param refTermIndex the term index
     * @return the term of the @ref property; or {@code null} if not found
     */
    private static @Nullable ITerm tryGetRefProperty(RRSolverState state, TermIndex refTermIndex) {
        final ITermProperty property = state.getState().termProperties().get(Tuple2.of(refTermIndex, B.newAppl("Ref")));
        return (property != null ? property.value() : null);
    }

    private static <T> T checkNotNull(@Nullable T value, String message) {
        if (value == null) throw new NullPointerException(message);
        return value;
    }

    /**
     * Takes a term application and unwraps it by recreating the term application
     * but with placeholders for the subterms, and adds placeholder descriptors to
     * the state for each subterm placeholder.
     *
     * @param engine the Tego engine
     * @param v the placeholder variable to which the unwrapped term should be bound
     * @param term the term application to unwrap
     * @param state the starting state
     * @param contextTerms the context terms
     * @return the new state
     */
    private static RRSolverState unwrapAppl(TegoEngine engine, ITermVar v, IApplTerm term, List<ITerm> contextTerms, RRSolverState state) {
        RRSolverState newState = state;
        final ArrayList<ITermVar> newSubterms = new ArrayList<>();
        for (ITerm a: term.getArgs()) {
            // fold(newPlaceholder) over the arguments
            final Pair<ITermVar, RRSolverState> newVarAndState = newPlaceholder(engine, newState, a, contextTerms);
            final ITermVar newVar = newVarAndState.component1();
            newState = newVarAndState.component2();
            newSubterms.add(newVar);
        }
        final IApplTerm newApplTerm = B.newAppl(term.getOp(), newSubterms);
        return bindToVar(newState, v, newApplTerm);
    }

    /**
     * Takes a list term and unwraps it by recreating the list term
     * but with placeholders for the elements, and adds placeholder descriptors to
     * the state for each element placeholder.
     *
     * @param engine the Tego engine
     * @param v the placeholder variable to which the unwrapped term should be bound
     * @param term the term application to unwrap
     * @param state the starting state
     * @param contextTerms the contexts
     * @return the new state
     */
    private static RRSolverState unwrapList(TegoEngine engine, ITermVar v, IListTerm term, List<ITerm> contextTerms, RRSolverState state) {
        RRSolverState newState = state;
        final ArrayList<ITermVar> newElements = new ArrayList<>();
        for (ITerm e: M.listElems().match(term).get()) {
            // fold(newPlaceholder) over the elements
            final Pair<ITermVar, RRSolverState> newVarAndState = newPlaceholder(engine, newState, e, contextTerms);
            final ITermVar newVar = newVarAndState.component1();
            newState = newVarAndState.component2();
            newElements.add(newVar);
        }
        final IListTerm newListTerm = B.newList(newElements);
        return bindToVar(newState, v, newListTerm);
    }

    /**
     * Creates a new placeholder.
     *
     * @param engine the Tego engine
     * @param state the starting state
     * @param term the term to put in the placeholder
     * @param contextTerms the contexts of the placeholder
     * @return a pair of the term variable for the placeholder and the new state
     */
    private static Pair<ITermVar, RRSolverState> newPlaceholder(TegoEngine engine, RRSolverState state, ITerm term, List<ITerm> contextTerms) {
        final NewPlaceholderStrategy newPlaceholder = NewPlaceholderStrategy.getInstance();
        @Nullable final Pair<ITermVar, RRSolverState> newVarAndState = engine.eval(newPlaceholder, RRPlaceholder.of(term, contextTerms), state);
        // TODO: Handle when newVarAndState == null?
        assert newVarAndState != null;
        return newVarAndState;
    }

    /**
     * Binds a term to a variable in the specified state, and returns a new state.
     *
     * @param state the initial state
     * @param v the placeholder variable to bind to
     * @param term the term to bind
     * @return the new state with the bound variable
     */
    private static RRSolverState bindToVar(RRSolverState state, ITermVar v, ITerm term) {
        // FIXME: Depends on existentials being passed down when replacing placeholders
        final ITermVar v2 = state.getExistentials().get(v);
        return state.withUpdatedConstraints(
            Collections.singleton(new CEqual((v2 != null ? v2 : v), term)),
            Collections.emptySet()
        );
    }

}

