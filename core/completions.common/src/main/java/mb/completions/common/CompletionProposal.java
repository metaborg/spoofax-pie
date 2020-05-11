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
    private final String description;
    private final String parameters;
    private final String type;
    private final String location;
    // documentation?
    private final StyleName kind;
    private final boolean deprecated;
    private final ListView<TextEdit> edits;

    /**
     * Initializes a new instance of the {@link CompletionProposal} class.
     *
     * @param label the label of the proposal
     * @param description the details of the proposal; or an empty string
     * @param parameters the parameters of the proposal; or an empty string
     * @param type the type of the proposal; or an empty string
     * @param location the location of the proposal; or an empty string
     * @param kind the kind of proposal
     * @param edits the edits to perform to insert the proposal
     * @param deprecated whether the proposal is deprecated
     */
    public CompletionProposal(String label, String description, String parameters, String type, String location, StyleName kind, ListView<TextEdit> edits, @Experimental boolean deprecated) {
        this.label = label;
        this.description = description;
        this.parameters = parameters;
        this.type = type;
        this.location = location;
        this.kind = kind;
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
     * Gets the description of the proposal,
     * such as a short description of a template,
     * the method being overridden,
     * or the field for which it is a getter.
     *
     * @return the description; or an empty string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the parameters of the proposal, such as type parameters and method parameters
     * including brackets and parentheses.
     *
     * @return the parameter string; or an empty string
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * Gets the type of the proposal, such as the field type or return type
     *
     * @return the type string; or an empty string
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the location string of the proposal, such as the namespace, package, or class.
     *
     * @return the location string; or an empty string
     */
    public String getLocation() {
        return location;
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
