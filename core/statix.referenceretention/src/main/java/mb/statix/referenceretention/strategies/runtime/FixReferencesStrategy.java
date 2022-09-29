package mb.statix.referenceretention.strategies.runtime;

import io.usethesource.capsule.Set;
import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.IAttachments;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.AbstractTerm;
import org.metaborg.util.functions.Action1;

import org.immutables.serial.Serial;
import org.immutables.value.Value;

import java.util.Objects;

public class FixReferencesStrategy {
}


/**
 * A reference retention descriptor for a placeholder.
 */
final class ReferenceRetentionPlaceholderDescriptor {
    private final ITerm ast;
    private final ITerm context;

    /**
     * Gets the AST inside the placeholder.
     * @return the AST, which may include {@link LockedReference} terms.
     */
    public ITerm getAst() {
        return ast;
    }

    /**
     * Gets the context for the references inside the placeholder's AST.
     * @return the context, which is a term that describes the context reference,
     * such as {@code Var("x")} or {@code Member("x", Var("y"))}.
     */
    public ITerm getContext() {
        return context;
    }

    /**
     * Initializes a new instance of the {@link ReferenceRetentionPlaceholderDescriptor} class.
     * @param ast the AST inside the placeholder
     * @param context the context for reference in the placeholder's AST
     */
    public ReferenceRetentionPlaceholderDescriptor(ITerm ast, ITerm context) {
        this.ast = ast;
        this.context = context;
    }

    @Override public boolean equals(Object other) {
        if(this == other) return true;
        if(!(other instanceof ReferenceRetentionPlaceholderDescriptor)) return false;
        ReferenceRetentionPlaceholderDescriptor that = (ReferenceRetentionPlaceholderDescriptor)other;
        // @formatter:off
        return Objects.equals(this.getAst(), that.getAst())
            && Objects.equals(this.getContext(), that.getContext());
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(ast, context);
    }

    @Override
    public String toString() {
        return "[[" + getAst() + "|" + getContext() + "]]";
    }
}

// FIXME: This won't work, the ITerm.Cases class doesn't support it? (I added otherwise())
@Value.Immutable(lazyhash = false)
@Serial.Version(value = 42L)
abstract class ALockedReference extends AbstractTerm {

    @Value.Parameter public abstract String getName();
    @Value.Parameter public abstract Object getDeclaration();   // FIXME: What type is a declaration?

    @Override public <T> T match(Cases<T> cases) {
        return cases.caseOtherwise(this);
    }

    @Override public <T, E extends Throwable> T matchOrThrow(CheckedCases<T, E> cases) throws E {
        return cases.caseOtherwise(this);
    }

    private volatile int hashCode;

    @Override public int hashCode() {
        int result = hashCode;
        if(result == 0) {
            result = Objects.hash(
                getName(),
                getDeclaration()
            );
            hashCode = result;
        }
        return result;
    }

    @Override public boolean equals(Object other) {
        if(this == other) return true;
        if(!(other instanceof ALockedReference)) return false;
        ALockedReference that = (ALockedReference)other;
        if(this.hashCode() != that.hashCode()) return false;
        // @formatter:off
        return Objects.equals(this.getName(), that.getName())
            && Objects.equals(this.getDeclaration(), that.getDeclaration());
        // @formatter:on
    }

    @Override public String toString() {
        return super.toString();
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
@Stratego("get-reference")
external def getReference(term: ITerm): ITerm?

@Stratego("qualify-reference")
external def qualifyReference(reference: Iterm, context: ITerm): ITerm

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
    let subterm: ITerm = descriptor.ast in
    let reference: ITerm? = <try(getReference)> subterm in  // TODO: Get reference if it is a LockedReference
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
