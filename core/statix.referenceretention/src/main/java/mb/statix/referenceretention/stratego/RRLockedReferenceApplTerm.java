package mb.statix.referenceretention.stratego;

import mb.statix.referenceretention.statix.RRLockedReference;
import mb.statix.scopegraph.Scope;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.TermFactory;

import java.util.Iterator;
import java.util.List;

/**
 * A locked reference is a reference for which the declaration it refers to is known
 * and should not change.
 * <p>
 * A reference is resolved to a specific declaration scope using Statix. After this resolution, moving the reference
 * (e.g., when inlining a method body), this could invalidate the reference. To prevent this, the reference syntax and
 * the declaration scope it resolved to are wrapped in this term.
 */
@Deprecated
public final class RRLockedReferenceApplTerm extends StrategoAppl {

    private final IStrategoTerm term;
    private final Scope declaration;

    /**
     * Gets the term that represents the locked reference.
     * @return a term
     */
    public IStrategoTerm getTerm() { return term; }

    /**
     * Gets the declaration to which this locked reference resolved.
     * @return a declaration, represented by the declaration's unique scope
     */
    public Scope getDeclaration() { return declaration; }

    public RRLockedReferenceApplTerm(IStrategoTerm term, Scope declaration, ITermFactory factory) {
        // Note: we don't make the input terms children of the ApplTerm, because I'm not sure if it's needed.
        super(factory.makeConstructor("___RRLockedReference___", 0), new IStrategoTerm[0], TermFactory.EMPTY_LIST);
        assert term != null;
        assert declaration != null;
        this.term = term;
        this.declaration = declaration;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("\uD83D\uDD12");
        sb.append(term);
        sb.append(" -> ");
        sb.append(declaration);
        sb.append("]");
        return sb.toString();
    }

}
