package mb.statix;

import mb.common.codecompletion.CodeCompletionItem;
import mb.common.editing.TextEdit;
import mb.common.style.StyleName;
import mb.common.util.ListView;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.Objects;

/**
 * A completion proposal that includes the Stratego term.
 */
public class StrategoTermCodeCompletionItem extends CodeCompletionItem {

    private final IStrategoTerm strategoTerm;

    /**
     * Initializes a new instance of the {@link CodeCompletionItem} class.
     *
     * @param strategoTerm        the Stratego term of the proposal
     * @param label       the label of the proposal
     * @param description the details of the proposal; or an empty string
     * @param parameters  the parameters of the proposal; or an empty string
     * @param type        the type of the proposal; or an empty string
     * @param location    the location of the proposal; or an empty string
     * @param kind        the kind of proposal
     * @param edits       the edits to perform to insert the proposal
     * @param deprecated  whether the proposal is deprecated
     */
    public StrategoTermCodeCompletionItem(IStrategoTerm strategoTerm, String label, String description, String parameters, String type, String location, StyleName kind, ListView<TextEdit> edits, boolean deprecated) {
        super(label, description, parameters, type, location, kind, edits, deprecated);
        this.strategoTerm = strategoTerm;
    }

    /**
     * The Stratego term of the proposal.
     * @return the Stratego term
     */
    public IStrategoTerm getStrategoTerm() {
        return strategoTerm;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        return innerEquals((StrategoTermCodeCompletionItem)o);
    }

    protected boolean innerEquals(StrategoTermCodeCompletionItem that) {
        return this.strategoTerm == that.strategoTerm
            && super.innerEquals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            strategoTerm
        ) + super.hashCode();
    }
}
