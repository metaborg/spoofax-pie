package mb.statix.referenceretention.tego;

import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.TermBuild;
import mb.statix.constraints.CEqual;
import mb.statix.referenceretention.statix.LockedReference;
import mb.statix.referenceretention.statix.RRLockedReference;
import mb.statix.referenceretention.statix.RRPlaceholder;
import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy3;
import mb.tego.strategies.Strategy1;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.tuples.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
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
        // Get all the strategies at the start
//        final NewPlaceholderStrategy newPlaceholderStrategy = NewPlaceholderStrategy.getInstance();
        final Strategy1</* ctx */ ITerm, /* term */ ITerm, /* result */ @Nullable ITerm> qualifyReference = ctx.getQualifyReferenceStrategy();

        // Construct the state without the placeholder being unwrapped/fixed
        final RRSolverState inputWithoutPlaceholder = input.removePlaceholder(v);

        // (body, contexts) <- descriptor,
        final ITerm term = descriptor.getBody();
        final List<ITerm> contexts = descriptor.getContexts();

        final @Nullable RRLockedReference lockedRef = RRLockedReference.matcher().match(term).orElse(null);
        if (lockedRef != null) {
            // The term is a locked reference
            if (contexts.size() == 0) {
                // A locked reference to declaration d with no context.
                // Check whether the resulting reference resolves to declaration d
                // ⟦ r^d | ε ⟧ = { r } when r |-> d else {}
                // Apply the context to the reference r^d and check whether the result is a valid locked reference to declaration d
                // Or should we do the check somewhere else?
                // TODO: Do the check whether the reference still points to the given declaration
                if (true /* TODO: if check succeeds */) {
                    // Check succeeded, reference is valid.
                    final RRSolverState newState = bindToVar(inputWithoutPlaceholder, v, lockedRef.getTerm());
                    return Seq.of(newState);
                } else {
                    // Check failed, reference is invalid.
                    return Seq.of();
                }
            } else if (contexts.size() == 1) {
                // A locked reference to declaration d with a single context.
                // Apply the context and create a placeholder with the new reference but without a context.
                // ⟦ r | c0 ⟧ = { ⟦ r' | ε ⟧ } where c0(r) == r' else {}
                final ITerm context = contexts.get(0);
                // This executes the context on the reference
                final @Nullable ITerm qreference = engine.eval(qualifyReference, context, lockedRef.getTerm());
                if(qreference != null) {
                    final RRSolverState newState = unwrap(engine, v, (IApplTerm)term, contexts, inputWithoutPlaceholder);
                    return Seq.of(newState);
                } else {
                    // No results.
                    return Seq.of();
                }
            } else {  // contexts.size() > 1
                // A locked reference with multiple contexts, split into multiple states each with a single context
                // ⟦ r | c0, c1, .. ⟧ = { ⟦ r | c0 ⟧, ⟦ r | c1 ⟧, .. }
                List<RRSolverState> newStates = new ArrayList<>(contexts.size() + 1);
                for(ITerm context : contexts) {
                    final Pair<ITermVar, RRSolverState> varAndstate = newPlaceholder(engine, inputWithoutPlaceholder, lockedRef, Collections.singletonList(context));
                    final ITermVar newVar = varAndstate.component1();
                    final RRSolverState newState = varAndstate.component2();    // TODO: Do we need to apply `bindToVar`?
                    newStates.add(newState);
                }
                return Seq.from(newStates);
            }
        } else if (term instanceof IApplTerm) {
            // The term is a term application, so we unwrap it once
            //   ⟦ T(a0, a1, ..) | C ⟧  ->  { T( ⟦ a0 | C ⟧, ⟦ a1 | C ⟧, .. ) }
            final RRSolverState finalState = unwrap(engine, v, (IApplTerm)term, contexts, inputWithoutPlaceholder);
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
     * Takes a term application and unwraps it by recreating the term application
     * but with placeholders for the subterms, and adds placeholder descriptors to
     * the state for each subterm placeholder.
     *
     * @param engine the Tego engine
     * @param v the placeholder variable to which the unwrapped term should be bound
     * @param term the term application to unwrap
     * @param state the starting state
     * @param contexts the contexts
     * @return the new state
     */
    private static RRSolverState unwrap(TegoEngine engine, ITermVar v, IApplTerm term, List<ITerm> contexts, RRSolverState state) {
        RRSolverState newState = state;
        final ArrayList<ITermVar> newSubterms = new ArrayList<>();
        for (ITerm a: term.getArgs()) {
            // fold(newPlaceholder) over the arguments
            final Pair<ITermVar, RRSolverState> newVarAndState = newPlaceholder(engine, newState, a, contexts);
            final ITermVar newVar = newVarAndState.component1();
            newState = newVarAndState.component2();
            newSubterms.add(newVar);
        }
        final IApplTerm newApplTerm = TermBuild.B.newAppl(term.getOp(), newSubterms);
        return bindToVar(newState, v, newApplTerm);
    }

    /**
     * Creates a new placeholder.
     *
     * @param engine the Tego engine
     * @param state the starting state
     * @param term the term to put in the placeholder
     * @param contexts the contexts of the placeholder
     * @return a pair of the term variable for the placeholder and the new state
     */
    private static Pair<ITermVar, RRSolverState> newPlaceholder(TegoEngine engine, RRSolverState state, ITerm term, List<ITerm> contexts) {
        final NewPlaceholderStrategy newPlaceholder = NewPlaceholderStrategy.getInstance();
        @Nullable final Pair<ITermVar, RRSolverState> newVarAndState = engine.eval(newPlaceholder, RRPlaceholder.of(term, contexts), state);
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

