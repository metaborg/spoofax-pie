package mb.statix.codecompletion;

import mb.common.codecompletion.CodeCompletionItem;
import mb.common.editing.TextEdit;
import mb.common.style.StyleName;
import mb.common.util.ListView;
import mb.nabl2.terms.ITerm;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.Objects;

/**
 * A completion proposal that includes the Stratego term.
 */
public class TermCodeCompletionItem extends StrategoTermCodeCompletionItem {

    private final ITerm term;

    /**
     * Initializes a new instance of the {@link CodeCompletionItem} class.
     *
     * @param term        the term of the proposal
     * @param strategoTerm the Stratego term of the proposal
     * @param label       the label of the proposal
     * @param description the details of the proposal; or an empty string
     * @param parameters  the parameters of the proposal; or an empty string
     * @param type        the type of the proposal; or an empty string
     * @param location    the location of the proposal; or an empty string
     * @param kind        the kind of proposal
     * @param edits       the edits to perform to insert the proposal
     * @param deprecated  whether the proposal is deprecated
     */
    public TermCodeCompletionItem(ITerm term, IStrategoTerm strategoTerm, String label, String description, String parameters, String type, String location, StyleName kind, ListView<TextEdit> edits, boolean deprecated) {
        super(strategoTerm, label, description, parameters, type, location, kind, edits, deprecated);
        this.term = term;
    }

    /**
     * The term of the proposal.
     * @return the term
     */
    public ITerm getTerm() {
        return term;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        return innerEquals((TermCodeCompletionItem)o);
    }

    protected boolean innerEquals(TermCodeCompletionItem that) {
        return this.term == that.term
            && super.innerEquals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            term
        ) + super.hashCode();
    }
}
