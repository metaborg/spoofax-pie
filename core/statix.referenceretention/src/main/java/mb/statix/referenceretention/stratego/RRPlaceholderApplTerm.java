package mb.statix.referenceretention.stratego;

import mb.statix.referenceretention.statix.RRLockedReference;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.TermFactory;

import java.util.Iterator;
import java.util.List;

/**
 * A reference retention placeholder term, that is, a placeholder of the form {@code [[ &lt;body&gt; | &lt;context&gt; ]]}.
 */
public final class RRPlaceholderApplTerm extends StrategoAppl {

    private final IStrategoTerm body;
    private final List<IStrategoTerm> context;

    /**
     * Gets the body term.
     * @return a term, which may include {@link RRLockedReference} terms
     */
    public IStrategoTerm getBody() { return body; }

    /**
     * Gets the context for the references inside the placeholder's AST.
     * @return the context, which is a term that describes the context reference,
     * such as {@code Var("x")} or {@code Member("x", Var("y"))}; or {@code null}
     */
    public List<IStrategoTerm> getContext() { return context; }

    public RRPlaceholderApplTerm(IStrategoTerm body, List<IStrategoTerm> context, ITermFactory factory) {
        // Note: we don't make the input terms children of the ApplTerm, because I'm not sure if it's needed.
        super(factory.makeConstructor("___RRPlaceholder___", 0), new IStrategoTerm[0], TermFactory.EMPTY_LIST);
        assert body != null;
        assert context != null;
        this.body = body;
        this.context = context;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[[");
        sb.append(body);
        sb.append("|");
        final Iterator<IStrategoTerm> iterator = context.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next());
            while (iterator.hasNext()) {
                sb.append(", ");
                sb.append(iterator.next());
            }
        } else {
            sb.append("ε");
        }
        sb.append("]]");
        return sb.toString();
    }

}
