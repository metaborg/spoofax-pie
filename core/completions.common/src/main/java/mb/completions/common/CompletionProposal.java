package mb.completions.common;

import mb.common.editing.TextEdit;
import mb.common.style.StyleName;
import mb.common.util.Experimental;
import mb.common.util.ListView;

import java.io.Serializable;

/**
 * A completion proposal.
 */
public final class CompletionProposal implements Serializable {

    private final String label;
    private final String details;
    private final StyleName kind;
    private final boolean deprecated;
    private final ListView<TextEdit> edits;

    /**
     * Initializes a new instance of the {@link CompletionProposal} class.
     *
     * @param label the label of the proposal
     * @param kind the kind of proposal
     * @param details the details of the proposal; or an empty string
     * @param edits the edits to perform to insert the proposal
     * @param deprecated whether the proposal is deprecated
     */
    public CompletionProposal(String label, StyleName kind, String details, ListView<TextEdit> edits, @Experimental boolean deprecated) {
        this.label = label;
        this.kind = kind;
        this.details = details;
        this.deprecated = deprecated;
        this.edits = edits;
    }

    /**
     * Gets the label to be displayed to the user.
     *
     * @return the label to display
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the details of the proposal, such as type information.
     *
     * @return the details; or an empty string
     */
    public String getDetails() {
        return details;
    }

    /**
     * Gets the kind of proposal.
     *
     * This is used to render the proposal in an IDE-specific way.
     *
     * @return the style name of the kind of proposal
     */
    public StyleName getKind() {
        return kind;
    }

    /**
     * Gets a list of text edits to perform when this proposal is inserted.
     *
     * Most proposals will only insert text at the caret location,
     * but some proposals might additionally require text to be inserted at
     * other locations in the document, such as adding an import statement
     * or a qualifier.
     *
     * @return a list of text edits, where the first edit is the primary one
     */
    public ListView<TextEdit> getEdits() {
        return edits;
    }

    /**
     * Gets whether the proposal proposes something that is deprecated.
     *
     * @return {@code true} when the proposed is deprecated;
     * otherwise, {@code false}.
     */
    @Experimental
    public boolean isDeprecated() {
        return deprecated;
    }

}
