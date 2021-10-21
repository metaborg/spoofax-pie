package mb.statix;

import mb.common.codecompletion.CodeCompletionItem;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.region.Region;
import mb.common.util.ListView;
import mb.nabl2.terms.ITermVar;

import java.util.Objects;

/**
 * A code completion result with the placeholder being completed.
 */
public class TermCodeCompletionResult extends CodeCompletionResult {
    private final ITermVar placeholder;

    /**
     * Initializes a new instance of the {@link CodeCompletionResult} class.
     *
     * @param proposals         the completion proposals, in the order in which
     *                          they are to be presented to the user
     * @param replacementRegion the region to replace with the completion
     * @param isComplete        whether the list of completions is complete
     */
    public TermCodeCompletionResult(ITermVar placeholder, ListView<CodeCompletionItem> proposals, Region replacementRegion, boolean isComplete) {
        super(proposals, replacementRegion, isComplete);
        this.placeholder = placeholder;
    }

    public ITermVar getPlaceholder() {
        return placeholder;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        return internalEquals((TermCodeCompletionResult)o);
    }

    protected boolean internalEquals(TermCodeCompletionResult that) {
        return this.placeholder.equals(that.placeholder)
            && super.internalEquals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            placeholder
        ) + super.hashCode();
    }

    @Override public String toString() {
        return "CodeCompletionResult{" +
            "proposals=" + getProposals() + ", " +
            "replacementRegion=" + getReplacementRegion() + ", " +
            "isComplete=" + isComplete() + ", " +
            "placeholder=" + placeholder +
            '}';
    }
}
