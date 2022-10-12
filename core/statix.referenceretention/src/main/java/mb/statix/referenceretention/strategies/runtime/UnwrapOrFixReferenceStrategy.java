package mb.statix.referenceretention.strategies.runtime;

import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.TermBuild;
import mb.statix.constraints.CEqual;
import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy3;
import mb.tego.strategies.Strategy1;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.tuples.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;

public final class UnwrapOrFixReferenceStrategy extends NamedStrategy3<RRContext, ITermVar, RRPlaceholderDescriptor, RRSolverState, Seq<RRSolverState>> {

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
        RRPlaceholderDescriptor descriptor,
        RRSolverState input
    ) {
        return eval(engine, ctx, v, descriptor, input);
    }

    /**
     * Fix a placeholder reference.
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
        RRPlaceholderDescriptor descriptor,
        RRSolverState input
    ) {
        // Get all the strategies at the start
        final NewPlaceholderStrategy newPlaceholder = NewPlaceholderStrategy.getInstance();
        final Strategy1<ITerm, LockedReference, @Nullable ITerm> qualifyReference = ctx.getQualifyReferenceStrategy();

        // (term, context) <- descriptor,
        final ITerm term = descriptor.getTerm();
        final @Nullable ITerm context = descriptor.getContext();
        if (term instanceof LockedReference) {
            if (context != null) {
                // The term is a locked reference with a context,
                // so we try both the qualified and unqualified reference.
                // "[[ r | c ]]" = { "x.[[ r |]]" , "[[ r |]]" }
                final @Nullable LockedReference reference = (LockedReference)term;
                final @Nullable ITerm qreference = engine.eval(qualifyReference, descriptor.getContext(), reference);
                @Nullable RRSolverState newStateQ = null;    // The state for the qualified reference "x.[[r |]]"; or `null` when it could not be constructed
                if (qreference != null) {
                    final Pair<IApplTerm, RRSolverState> newApplTermAndState = unwrap(engine, (IApplTerm)term, context, input);
                    final IApplTerm newApplTerm = newApplTermAndState.component1();
                    newStateQ = newApplTermAndState.component2();
                    newStateQ = newStateQ.withUpdatedConstraints(
                        Collections.singleton(new CEqual(v, newApplTerm)),
                        Collections.emptySet()
                    );
                }
                RRSolverState newStateUQ;            // The state for the unqualified reference "[[ r |]]"
                {
                    @Nullable final Pair<ITermVar, RRSolverState> newVarAndState = engine.eval(newPlaceholder, new RRPlaceholderDescriptor(reference, null /* No context */), input);
                    final ITermVar newVar = newVarAndState.component1();
                    final RRSolverState newState = newVarAndState.component2();
                    newStateUQ = newState.withUpdatedConstraints(
                        Collections.singleton(new CEqual(v, newVar)),
                        Collections.emptySet()
                    );
                }
                return Seq.ofNotNull(newStateQ, newStateUQ);
            } else {
                // The term is a locked reference without a context.
                // so we check whether it resolves correctly.
                // "[[ r |]]" = "r" if r still resolves to the same declaration
                throw new IllegalStateException("Not yet implemented!");
            }
        } else if (term instanceof IApplTerm) {
            // The term is a term application, so we unwrap it once
            // "[[ T(a0, a1, .., an) | c ]]" -> "T( [[ a0 | c ]], [[ a1 | c ]], .., [[ an | c ]] )"
            final Pair<IApplTerm, RRSolverState> newApplTermAndState = unwrap(engine, (IApplTerm)term, context, input);
            final IApplTerm newApplTerm = newApplTermAndState.component1();
            final RRSolverState newState = newApplTermAndState.component2();
            final RRSolverState finalState = newState.withUpdatedConstraints(
                Collections.singleton(new CEqual(v, newApplTerm)),
                Collections.emptySet()
            );
            return Seq.of(finalState);
        } else {
            // The term is not a term application and not a locked reference
            // "[[ t | _ ]]" -> "t"
            final RRSolverState newState = input.withUpdatedConstraints(
                Collections.singleton(new CEqual(v, term)),
                Collections.emptySet()
            );
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
     * @param term the term application to unwrap
     * @param state the starting state
     * @return a tuple of the new term application and the new state
     */
    private static Pair<IApplTerm, RRSolverState> unwrap(TegoEngine engine, IApplTerm term, @Nullable ITerm context, RRSolverState state) {
        final NewPlaceholderStrategy newPlaceholder = NewPlaceholderStrategy.getInstance();
        RRSolverState newState = state;
        final ArrayList<ITermVar> newSubterms = new ArrayList<>();
        for (ITerm a: term.getArgs()) {
            // fold(newPlaceholder) over the arguments
            @Nullable final Pair<ITermVar, RRSolverState> newVarAndState = engine.eval(newPlaceholder, new RRPlaceholderDescriptor(a, context), newState);
            final ITermVar newVar = newVarAndState.component1();
            newState = newVarAndState.component2();
            newSubterms.add(newVar);
        }
        final IApplTerm newApplTerm = TermBuild.B.newAppl(term.getOp(), newSubterms);
        return Pair.of(newApplTerm, newState);
    }

}

