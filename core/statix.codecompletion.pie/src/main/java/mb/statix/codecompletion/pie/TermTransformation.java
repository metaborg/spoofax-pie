package mb.statix.codecompletion.pie;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoPlaceholder;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * A term transformation.
 */
public abstract class TermTransformation {
    private final ITermFactory factory;

    /**
     * Initializes a new instance of the {@link TermTransformation} class.
     *
     * @param factory the term factory
     */
    protected TermTransformation(ITermFactory factory) {
        this.factory = factory;
    }

    /**
     * Performs a recursive bottom-up transformation of the specified term.
     *
     * The {@link #traverse} function determines whether the term is transformed and its subterms are traversed.
     * The {@link #transform} function transforms the term with the transformed subterms.
     *
     * @param term the term to transform
     * @return the recursively transformed term
     */
    public IStrategoTerm transformRecursive(IStrategoTerm term) {
        // TODO: Make this iterative instead of recursive.
        final boolean traverse = traverse(term);
        if (!traverse) return term;

        boolean changed = false;
        final IStrategoTerm[] newSubterms = new IStrategoTerm[term.getSubtermCount()];
        for(int i = 0; i < newSubterms.length; i++) {
            final IStrategoTerm oldSubterm = term.getSubterm(i);
            final IStrategoTerm newSubterm = transformRecursive(oldSubterm);
            changed |= newSubterm != oldSubterm;    // Reference equality.
            newSubterms[i] = newSubterm;
        }
        final IStrategoTerm newTerm = changed ? withSubterms(term, newSubterms) : term;
        return transform(newTerm);
    }

    /**
     * Returns whether the transformation should traverse the subterms of the given fragment.
     *
     * @param fragmentTerm the term to check
     * @return {@code true} to traverse the subterms of the given fragment;
     * otherwise, {@code false}
     */
    protected abstract boolean traverse(IStrategoTerm fragmentTerm);

    /**
     * Transforms a term.
     *
     * The subterms of this term have already been transformed.
     * This method is only called on terms for which {@link #traverse} returned {@code true}.
     *
     * @param fragmentTerm the term to transform
     * @return the transformed term; or the original term if nothing was transformed
     */
    protected abstract IStrategoTerm transform(IStrategoTerm fragmentTerm);

    /** Gets the term factory. */
    protected ITermFactory getTermFactory() {
        return this.factory;
    }

    /**
     * Creates a copy of the specified term, with its subterms replaced with the given array of subterms.
     *
     * @param term the term to copy
     * @param subterms the new subterms of the term
     * @return the modified copy of the term
     */
    protected IStrategoTerm withSubterms(IStrategoTerm term, IStrategoTerm[] subterms) {
        switch(term.getType()) {
            case APPL:
                final IStrategoAppl appl = (IStrategoAppl)term;
                if (subterms.length != appl.getSubtermCount())
                    throw new IllegalArgumentException("Expected " + appl.getSubtermCount() + " subterms, got " + subterms.length + ".");
                return factory.makeAppl(appl.getConstructor(), subterms, appl.getAnnotations());
            case TUPLE:
                final IStrategoTuple tuple = (IStrategoTuple)term;
                if (subterms.length != tuple.getSubtermCount())
                    throw new IllegalArgumentException("Expected " + tuple.getSubtermCount() + " subterms, got " + subterms.length + ".");
                return factory.makeTuple(subterms, tuple.getAnnotations());
            case LIST:
                final IStrategoList list = (IStrategoList)term;
                return factory.makeList(subterms, list.getAnnotations());
            case PLACEHOLDER:
                final IStrategoPlaceholder placeholder = (IStrategoPlaceholder)term;
                return factory.annotateTerm(factory.makePlaceholder(subterms[0]), placeholder.getAnnotations());
            case INT:
            case REAL:
            case STRING:
            case CTOR:
            case BLOB:
                if (subterms.length > 0)
                    throw new IllegalArgumentException("Expected 0 subterms, got " + subterms.length + ".");
                return term;
            default:
                throw new IllegalArgumentException("Unsupported type of term: " + term.getType());
        }
    }
}
