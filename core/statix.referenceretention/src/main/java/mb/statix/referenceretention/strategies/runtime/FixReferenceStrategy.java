package mb.statix.referenceretention.strategies.runtime;

import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.codecompletion.SolverState;
import mb.statix.constraints.CEqual;
import mb.tego.sequences.Seq;
import mb.tego.strategies.NamedStrategy1;
import mb.tego.strategies.NamedStrategy3;
import mb.tego.strategies.Strategy1;
import mb.tego.strategies.runtime.TegoEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;

public final class FixReferenceStrategy extends NamedStrategy3<ReferenceRetentionContext, ITermVar, ReferenceRetentionPlaceholderDescriptor, SolverState, Seq<SolverState>> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final FixReferenceStrategy instance = new FixReferenceStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static FixReferenceStrategy getInstance() { return (FixReferenceStrategy)instance; }

    private FixReferenceStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "fixReference";
    }

    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "RedundantSuppression"})
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "v";
            case 1: return "visitedInjections";
            case 2: return "descriptor";
            default: return super.getParamName(index);
        }
    }

    @Override
    public Seq<SolverState> evalInternal(
        TegoEngine engine,
        ReferenceRetentionContext ctx,
        ITermVar v,
        ReferenceRetentionPlaceholderDescriptor descriptor,
        SolverState input
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
    public static Seq<SolverState> eval(
        TegoEngine engine,
        ReferenceRetentionContext ctx,
        ITermVar v,
        ReferenceRetentionPlaceholderDescriptor descriptor,
        SolverState input
    ) {
        /*
        let subterm: ITerm = descriptor.ast in
        let reference: LockedReference? = <as[LockedReference]> subterm in  // TODO: Get reference if it is a LockedReference
        if reference then
            // Try to fix the reference, first by trying a qualified reference
            let qreference: ITerm = qualifyReference(reference, descriptor.context) in
            <first> (
                // Either try the qualified reference,
                let newAst = input.union(v to qreference) in
                let newState = <solve> newAst in
                let newDeclaration = <get-declaration(newState)> qreference in
                newDeclaration; ?reference.declaration
            ,
                // Or try the unqualified reference,
                // and pick the first that succeeds
                let newAst = input.union(v to reference) in
                let newState = <solve> newAst in
                let newDeclaration = <get-declaration(newState)> qreference in
                newDeclaration; ?reference.declaration
            )
        else
            // Unwrap
            TODO()
         */
        // Get all the strategies at the start
        final Strategy1<ITerm, ITerm, @Nullable ITerm> qualifyReference = ctx.getQualifyReferenceStrategy();

        final ITerm ast = descriptor.getAst();
        final @Nullable LockedReference reference = (ast instanceof LockedReference ? (LockedReference)ast : null);
        if (reference != null) {
            // TODO: Use apply()
            final @Nullable ITerm qreference = qualifyReference.evalInternal(engine, descriptor.getContext(), reference.getTerm());
            if (qreference == null) throw new IllegalStateException("qualifyReference returned null");   // TODO: Handle this case, return null?
            // Try the qualified reference and, if it succeeds, get its declaration
            // Compare the declaration to the expected declaration
            // Otherwise, if this comparison fails,
            // Try the unqualified reference and, if it succeeds, get its declaration
            // Compare the declaration to the expected declaration
            // Otherwise, if this comparison fails,
            // Fail.
            // TODO
        } else {
            // Unwrap
            final @Nullable IApplTerm applTerm = (ast instanceof IApplTerm ? (IApplTerm)ast : null);
            if (applTerm != null) {
                // Replace the subterms of applTerm with placeholders
                // and union v |-> applTerm (with the placeholders)
                // and store the placeholder mapping
                // TODO
            } else {
                // Union v |-> ast and be done
                final SolverState newState = input.withUpdatedConstraints(
                    Collections.singleton(new CEqual(v, ast)),
                    Collections.emptySet()
                );
            }
        }
        // TODO

        throw new IllegalStateException("Not implemented yet");
    }

}


// Not used, replaced with ReferenceRetentionContext.qualifyReferenceStrategy
final class QualifyReferenceStrategy extends NamedStrategy1<ITerm, ITerm, @Nullable ITerm> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final QualifyReferenceStrategy instance = new QualifyReferenceStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static QualifyReferenceStrategy getInstance() { return (QualifyReferenceStrategy)instance; }

    private QualifyReferenceStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    @Override
    public String getName() {
        return "qualifyReference";
    }

    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "RedundantSuppression"})
    @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "referenceContext";
            default: return super.getParamName(index);
        }
    }

    @Override
    public @Nullable ITerm evalInternal(
        TegoEngine engine,
        ITerm referenceContext,
        ITerm input
    ) {
        return eval(engine, referenceContext, input);
    }

    /**
     * Qualifies a reference.
     *
     * @param engine the Tego engine
     * @param referenceContext the context to qualify the reference with
     * @param input the reference to qualify
     * @return the qualified reference; or {@code null} when the reference cannot be qualified
     */
    public static @Nullable ITerm eval(
        TegoEngine engine,
        ITerm referenceContext,
        ITerm input
    ) {
        throw new IllegalStateException("Not implemented.");
    }
}





/*
Fix references
==============

Input: an AST with metavariables instead of placeholders,
and for each placeholder, a ReferenceRetentionPlaceholderDescriptor
describing the placeholder.

Output: an AST with metavariables replaced by placeholders,
but likely less metavariables than before and most (all?) placeholders fixed.

Algorithm:

// Stratego strategies:
@Stratego("qualify-reference")
external def qualifyReference(reference: ITerm, context: ITerm): ITerm

external def as[T](term: ITerm): T?

def T.try[T](f: T.() -> T?): T = f <+ id

def fixReferences(ast: ITerm, descriptors: Map<ITermVar, ReferenceRetentionPlaceholderDescriptor>): ITerm =

    let ast' = fixReferences(ast, descriptors) in
    let ast'' = fixReferences(ast', descriptors) in
    ast''

/// Fix a placeholder reference.
///
/// @param v the placeholder variable
/// @param descriptor the descriptor for the placeholder
/// @return the fixed reference; or `null` if it could not be fixed
def fixReference(input: SolverState, v: ITermVar, descriptor: ReferenceRetentionPlaceholderDescriptor): ITerm? =


 */
